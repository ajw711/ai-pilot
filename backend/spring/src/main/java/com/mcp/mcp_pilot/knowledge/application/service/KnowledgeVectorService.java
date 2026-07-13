package com.mcp.mcp_pilot.knowledge.application.service;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.vector.service.VectorMemoryService;
import com.mcp.mcp_pilot.knowledge.application.event.KnowledgeProcessedEvent;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;
import com.mcp.mcp_pilot.knowledge.exception.KnowledgeNotFoundException;
import com.mcp.mcp_pilot.knowledge.port.in.VectorUseCase;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgePersistencePort;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeVectorService implements VectorUseCase {

    private final VectorMemoryService vectorMemoryService;
    private final KnowledgePersistencePort persistencePort;
    private final MeterRegistry meterRegistry;

    @Override
    public void execute(KnowledgeProcessedEvent event) {
        // Lag 측정
        Duration lag = Duration.between(event.publishedAt(), Instant.now());
        meterRegistry.timer("knowledge_event_lag_seconds", "consumer", "vector").record(Duration.between(event.publishedAt(), Instant.now()));

        Timer.Sample sample = Timer.start(meterRegistry);
        String status = "success";

        // 1. Idempotency Check
        if (vectorMemoryService.exists(VectorTargetType.KNOWLEDGE, event.knowledgeId())) {
            log.info("[VectorService] 이미 벡터화된 데이터입니다. - ID: {}", event.knowledgeId());
            return;
        }

        log.info("[VectorService] 지식 벡터화 시작 - ID: {}", event.knowledgeId());
        persistencePort.updateStatus(event.knowledgeId(), KnowledgeStatus.VECTOR_INDEXING);

        try {
            persistencePort.findById(event.knowledgeId()).ifPresentOrElse(knowledge -> {

                float[] vector = vectorMemoryService.generateVectorOnly(
                        VectorTargetType.KNOWLEDGE,
                        knowledge.getId(),
                        knowledge.getFormattedContent()
                );

                vectorMemoryService.saveEmbedding(
                        VectorTargetType.KNOWLEDGE,
                        knowledge.getId(),
                        vector
                );

                boolean vectorDone = persistencePort.isPublished(event.knowledgeId());
                if (vectorDone) {
                    persistencePort.updateStatus(event.knowledgeId(), KnowledgeStatus.PUBLISHED);
                    log.info("[VectorService] Notion & Vector 모두 적재 완료 -> PUBLISHED 상태로 전환 (ID: {})", event.knowledgeId());
                } else {
                    if (knowledge.getStatus() == KnowledgeStatus.FAILED_AT_NOTION_PUBLISH) {
                        log.info("[VectorService] Vector 적재 완료 되었으나 Notion 발행 실패 상태이므로 상태 유지 (ID: {})", event.knowledgeId());
                    } else {
                        persistencePort.updateStatus(event.knowledgeId(), KnowledgeStatus.VECTOR_INDEXING);
                        log.info("[VectorService] Vector 적재 완료 (Notion 대기) -> VECTOR_INDEXING 상태 유지 (ID: {})", event.knowledgeId());
                    }
                }

            }, () -> {
                throw new KnowledgeNotFoundException(event.knowledgeId());
            });
        } catch (Exception e) {
            status = "fail";
            log.error("[VectorService] 벡터화 실패 (ID: {}): {}", event.knowledgeId(), e.getMessage());
            persistencePort.updateStatus(event.knowledgeId(), KnowledgeStatus.FAILED_AT_VECTOR_INDEX);
            throw e;
        } finally {
            meterRegistry.counter("vector_embedding_requests_total", "status", status).increment();
            sample.stop(meterRegistry.timer("vector_embedding_duration_seconds"));

            // E2E Latency 측정
            meterRegistry.timer("knowledge_event_e2e_seconds", "consumer", "vector")
                    .record(Duration.between(event.publishedAt(), Instant.now()));
        }
    }
}

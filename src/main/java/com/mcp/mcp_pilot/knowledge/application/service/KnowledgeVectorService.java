package com.mcp.mcp_pilot.knowledge.application.service;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.vector.service.VectorMemoryService;
import com.mcp.mcp_pilot.knowledge.application.event.KnowledgeProcessedEvent;
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

        try {
            persistencePort.findById(event.knowledgeId()).ifPresentOrElse(knowledge -> {
                vectorMemoryService.saveEmbedding(
                        VectorTargetType.KNOWLEDGE,
                        knowledge.getId(),
                        knowledge.getSummarizedContent()
                );
            }, () -> {
                throw new KnowledgeNotFoundException(event.knowledgeId());
            });
        } catch (Exception e) {
            status = "fail";
            log.error("[VectorService] 벡터화 실패 (ID: {}): {}", event.knowledgeId(), e.getMessage());
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

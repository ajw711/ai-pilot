package com.mcp.mcp_pilot.knowledge.application.service;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.vector.dto.RawChunk;
import com.mcp.mcp_pilot.ai.vector.port.VectorIndexingUseCase;
import com.mcp.mcp_pilot.knowledge.application.chunker.MarkdownChunker;
import com.mcp.mcp_pilot.knowledge.application.event.KnowledgeProcessedEvent;
import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;
import com.mcp.mcp_pilot.knowledge.exception.KnowledgeNotFoundException;
import com.mcp.mcp_pilot.knowledge.port.in.VectorUseCase;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgeEventPublishPort;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgePersistencePort;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgeVectorPort;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeVectorService implements VectorUseCase {

    private final VectorIndexingUseCase vectorIndexingUseCase;
    private final MarkdownChunker markdownChunker;
    private final KnowledgeEventPublishPort knowledgeEventPublishPort;
    private final KnowledgePersistencePort persistencePort;
    private final KnowledgeVectorPort knowledgeVectorPort;
    private final MeterRegistry meterRegistry;

    @Override
    public void execute(KnowledgeProcessedEvent event) {
        if (knowledgeVectorPort.isVectorStored(event.knowledgeId())) {
            log.info("[VectorService] 이미 저장된 벡터입니다. 건너뜁니다. - ID: {}",
                    event.knowledgeId());

            // 저장은 생략하고 기존 집계가 완료 여부를 다시 확인하도록 전달
            knowledgeEventPublishPort.publish(
                    "knowledge.vector.indexed",
                    event.knowledgeId()
            );
            return;
        }



        // Lag 측정
        Duration lag = Duration.between(event.publishedAt(), Instant.now());
        meterRegistry.timer("knowledge_event_lag_seconds", "consumer", "vector").record(Duration.between(event.publishedAt(), Instant.now()));

        Timer.Sample sample = Timer.start(meterRegistry);
        String status = "success";

        log.info("[VectorService] 지식 벡터화 시작 - ID: {}", event.knowledgeId());
        persistencePort.updateStatus(event.knowledgeId(), KnowledgeStatus.VECTOR_INDEXING);

        try {
            persistencePort.findById(event.knowledgeId()).ifPresentOrElse(knowledge -> {

                // 마크다운 본문을 헤딩/토큰 기준으로 청킹
                List<RawChunk> chunks = markdownChunker.chunk(
                        knowledge.getTitle(),
                        knowledge.getFormattedContent()
                );

                // 공통 인덱싱 엔진에 1:N 청크 일괄 색인 위임
                vectorIndexingUseCase.indexChunks(
                        VectorTargetType.KNOWLEDGE,
                        knowledge.getId(),
                        chunks
                );
                log.info("[VectorService] Vector 적재 완료 - Outbox 이벤트 발행 (ID: {})", event.knowledgeId());
                knowledgeEventPublishPort.publish("knowledge.vector.indexed", event.knowledgeId());

            }, () -> {
                throw new KnowledgeNotFoundException(event.knowledgeId());
            });
        } catch (Exception e) {
            status = "fail";
            log.error("[VectorService] 벡터화 실패 (ID: {}): {}", event.knowledgeId(), e.getMessage());
            persistencePort.updateStatus(event.knowledgeId(), KnowledgeStatus.FAILED_AT_VECTOR_INDEX);
            knowledgeEventPublishPort.publish("knowledge.vector.failed", event.knowledgeId());
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

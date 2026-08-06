package com.mcp.mcp_pilot.knowledge.application.service;

import com.mcp.mcp_pilot.knowledge.application.event.KnowledgeProcessedEvent;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;
import com.mcp.mcp_pilot.knowledge.exception.KnowledgeNotFoundException;
import com.mcp.mcp_pilot.knowledge.exception.KnowledgePublishException;
import com.mcp.mcp_pilot.knowledge.port.in.NotionUseCase;
import com.mcp.mcp_pilot.knowledge.port.out.*;
import com.mcp.mcp_pilot.knowledge.port.out.dto.NotionPublishResult;
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
public class KnowledgePublishService implements NotionUseCase {

    private final KnowledgePersistencePort persistencePort;
    private final KnowledgeEventPublishPort knowledgeEventPublishPort;
    private final NotionPublishPort notionPublishPort;
    private final MeterRegistry meterRegistry;

    /**
     * event lost tracking 아직 없음 필요성이 있는지.. 해당 플젝에서
     */
    @Override
    public void execute(KnowledgeProcessedEvent event) {

        // Lag 측정
        Duration lag = Duration.between(event.publishedAt(), Instant.now());
        meterRegistry.timer("knowledge_event_lag_seconds", "consumer", "notion").record(lag);

        // 멱등성 체크
        if (persistencePort.isPublished(event.knowledgeId())) {
            log.info("[NotionService] 이미 발행된 지식입니다. 건너뜁니다. - ID: {}", event.knowledgeId());
            return;
        }

        log.info("[NotionService] 노션 발행 유스케이스 실행 - ID: {} ", event.knowledgeId());
        persistencePort.updateStatus(event.knowledgeId(), KnowledgeStatus.NOTION_PUBLISHING);
        
        Timer.Sample sample = Timer.start(meterRegistry);
        String status = "success";
        String errorType = "none";
        
        try {
            KnowledgeLog knowledgeLog = persistencePort.findById(event.knowledgeId())
                    .orElseThrow(() -> new KnowledgeNotFoundException(event.knowledgeId()));
            
            List<String> tags = persistencePort.findTagsByKnowledgeId(event.knowledgeId());
            NotionPublishResult result = notionPublishPort.publish(knowledgeLog, tags);
            log.info("[NotionService] 노션 발행 성공 - Page ID: {}", result.pageId());
            
            // 결과 영속화
            persistencePort.updatePublicationResult(event.knowledgeId(), result.pageId(), result.pageUrl());

            log.info("[NotionService] Notion 적재 완료 - Outbox 이벤트 발행 (ID: {})", event.knowledgeId());
            knowledgeEventPublishPort.publish("knowledge.notion.published", event.knowledgeId());

        } catch (Throwable e) {
            status = "fail";
            errorType = classifyError(e);
            log.error("[NotionService] 노션 발행 실패 (ID: {}): {}", event.knowledgeId(), e.getMessage());
            persistencePort.updateStatus(event.knowledgeId(), KnowledgeStatus.FAILED_AT_NOTION_PUBLISH);
            knowledgeEventPublishPort.publish("knowledge.notion.failed", event.knowledgeId());
            throw new KnowledgePublishException(e);
        } finally {
            // <domain>_<subsystem>_<metric>_<unit> 메트릭 패턴 적용
            // https://oneuptime.com/blog/post/2026-01-30-metric-naming-conventions/view
            // 성공/실패 횟수 및 API 응답 시간
            meterRegistry.counter("notion_publish_requests_total", "status", status, "error_type", errorType)
                    .increment();
            sample.stop(meterRegistry.timer("notion_publish_latency_seconds"));

            // E2E Latency (발행부터 완료까지 전체 유저 체감 시간)
            Duration e2e = Duration.between(event.publishedAt(), Instant.now());
            meterRegistry.timer("knowledge_event_e2e_seconds", "consumer", "notion").record(e2e);
        }
    }

    private String classifyError(Throwable e) {
        String message = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        if (message.contains("timeout")) return "timeout";
        if (message.contains("429")) return "rate_limit";
        if (message.contains("401") || message.contains("403")) return "auth_error";
        if (message.contains("5xx")) return "server_error";
        return "unknown";
    }
}

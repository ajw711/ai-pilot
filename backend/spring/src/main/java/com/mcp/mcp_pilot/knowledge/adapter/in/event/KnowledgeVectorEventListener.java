package com.mcp.mcp_pilot.knowledge.adapter.in.event;

import com.mcp.mcp_pilot.knowledge.application.event.KnowledgeProcessedEvent;
import com.mcp.mcp_pilot.knowledge.port.in.VectorUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeVectorEventListener {

    private final VectorUseCase vectorUseCase;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onKnowledgeProcessed(KnowledgeProcessedEvent event) {
        log.info("[Vector-Consumer] 벡터화 작업 시작 - ID: {}", event.knowledgeId());
        try {
            vectorUseCase.execute(event);
        } catch (Exception e) {
            log.error("[Vector-Consumer] 벡터화 실패 (ID: {}): {}", event.knowledgeId(), e.getMessage());
        }
    }
}

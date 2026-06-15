package com.mcp.mcp_pilot.knowledge.application.service;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.vector.service.VectorMemoryService;
import com.mcp.mcp_pilot.knowledge.application.event.KnowledgeProcessedEvent;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgePersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeVectorListener {

    private final VectorMemoryService vectorMemoryService;
    private final KnowledgePersistencePort knowledgePersistencePort;

    /**
     * Knowledge 도메인에서 가공이 완료(커밋)된 후 발행되는 이벤트를 수신하여 벡터화를 수행
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(KnowledgeProcessedEvent event) {
        log.info("[Vector-Listener] 이벤트 수신. 벡터화 시작 (ID: {})", event.knowledgeId());
        knowledgePersistencePort.findById(event.knowledgeId()).ifPresent(knowledge -> {
            vectorMemoryService.saveEmbedding(
                    VectorTargetType.KNOWLEDGE,
                    knowledge.getId(),
                    knowledge.getSummarizedContent()
            );
            log.info("[Vector-Listener]  지식 벡터화 완료 (ID: {})", event.knowledgeId());
        });
    }
}

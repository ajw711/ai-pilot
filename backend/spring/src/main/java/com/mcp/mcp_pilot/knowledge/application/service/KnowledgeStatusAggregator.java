package com.mcp.mcp_pilot.knowledge.application.service;

import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgePersistencePort;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgeVectorPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeStatusAggregator {

    private final KnowledgePersistencePort persistencePort;
    private final KnowledgeVectorPort knowledgeVectorPort;

    @Transactional
    public void recompute(Long knowledgeId) {
        KnowledgeLog knowledge = persistencePort.findById(knowledgeId).orElse(null);
        if (knowledge == null) {
            log.warn("[StatusAggregator] 존재하지 않는 KnowledgeId: {}", knowledgeId);
            return;
        }

        boolean notionPublished = persistencePort.isPublished(knowledgeId);
        boolean vectorStored = knowledgeVectorPort.isVectorStored(knowledgeId);

        log.info("[StatusAggregator] 상태 재계산 시작 - KnowledgeId: {}, NotionPublished: {}, VectorStored: {}, CurrentStatus: {}",
                knowledgeId, notionPublished, vectorStored, knowledge.getStatus());

        // Notion과 Vector 적재가 둘 다 완수되었을 때만 최종 PUBLISHED 로 전환
        if (notionPublished && vectorStored) {
            persistencePort.updateStatus(knowledgeId, KnowledgeStatus.PUBLISHED);
            log.info("[StatusAggregator] Notion & Vector 모두 적재 완료 -> PUBLISHED 전환 (ID: {})", knowledgeId);
        }
        // 이미 명시적으로 서브시스템 catch 블록에서 실패로 기록된 상태 보존
        else if (knowledge.getStatus() == KnowledgeStatus.FAILED_AT_NOTION_PUBLISH) {
            log.info("[StatusAggregator] Notion 발행 실패 상태 명시적 보존 -> FAILED_AT_NOTION_PUBLISH 유지 (ID: {})", knowledgeId);
        }
        else if (knowledge.getStatus() == KnowledgeStatus.FAILED_AT_VECTOR_INDEX) {
            log.info("[StatusAggregator] Vector 인덱싱 실패 상태 명시적 보존 -> FAILED_AT_VECTOR_INDEX 유지 (ID: {})", knowledgeId);
        }
        // 한쪽만 진행 중이거나 아직 수행 대기 중인 경우 성급히 실패로 바꾸지 않고 상태 유지
        else {
            log.info("[StatusAggregator] 파이프라인 진행 중 상태 유지 (NotionPublished: {}, VectorStored: {}, CurrentStatus: {}) (ID: {})",
                    notionPublished, vectorStored, knowledge.getStatus(), knowledgeId);
        }
    }
}

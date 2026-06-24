package com.mcp.mcp_pilot.knowledge.application.service;

import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;
import com.mcp.mcp_pilot.knowledge.exception.KnowledgeNotFoundException;
import com.mcp.mcp_pilot.knowledge.port.in.DeleteKnowledgeUseCase;
import com.mcp.mcp_pilot.knowledge.port.in.dto.DeleteKnowledgeResult;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgePersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeDeleteService implements DeleteKnowledgeUseCase {

    private final KnowledgePersistencePort persistencePort;

    @Override
    @Transactional
    public DeleteKnowledgeResult delete(Long knowledgeId) {
        log.info("[DeleteService] 지식 소프트 딜리트 실행 - ID: {}", knowledgeId);

        KnowledgeLog knowledge  = persistencePort.findById(knowledgeId)
                .orElseThrow(() -> new KnowledgeNotFoundException(knowledgeId));
        LocalDateTime now = LocalDateTime.now();
        knowledge.delete(now);
        persistencePort.save(knowledge);
        return DeleteKnowledgeResult.of(knowledgeId, knowledge.getStatus(), now);
    }
}

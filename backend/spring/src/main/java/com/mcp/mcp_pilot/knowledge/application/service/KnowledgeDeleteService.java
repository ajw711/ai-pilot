package com.mcp.mcp_pilot.knowledge.application.service;

import com.mcp.mcp_pilot.knowledge.port.in.DeleteKnowledgeUseCase;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgePersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeDeleteService implements DeleteKnowledgeUseCase {

    private final KnowledgePersistencePort persistencePort;

    @Override
    public void delete(Long knowledgeId) {
        log.info("[DeleteService] 지식 소프트 딜리트 실행 - ID: {}", knowledgeId);
        persistencePort.delete(knowledgeId);
    }
}

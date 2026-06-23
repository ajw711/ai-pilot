package com.mcp.mcp_pilot.knowledge.application.service;

import com.mcp.mcp_pilot.knowledge.application.event.KnowledgeProcessedEvent;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.exception.KnowledgeNotFoundException;
import com.mcp.mcp_pilot.knowledge.port.in.dto.ApproveKnowledgeCommand;
import com.mcp.mcp_pilot.knowledge.port.out.ApproveKnowledgeUseCase;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgePersistencePort;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgeSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeApproveService implements ApproveKnowledgeUseCase {

    private final KnowledgePersistencePort persistencePort;
    private final KnowledgeSearchPort searchPort;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public void approve(ApproveKnowledgeCommand command) {
        Long knowledgeId = command.knowledgeId();
        log.info("[ApproveService] 지식 승인 프로세스 시작 - ID: {}", command.knowledgeId());

        KnowledgeLog knowledge = searchPort.findById(command.knowledgeId())
                .orElseThrow(() -> new KnowledgeNotFoundException(command.knowledgeId()));

        knowledge.approve(command.finalFormattedContent());

        persistencePort.save(knowledge);

        // 외부 채널(Notion/Vector Store) 발행을 시작하는 이벤트 발행
        applicationEventPublisher.publishEvent(KnowledgeProcessedEvent.of(knowledgeId));
        log.info("[ApproveService] 지식 최종 승인 완료 및 발행 이벤트 발행 성공 (ID: {})", knowledgeId);
    }
}

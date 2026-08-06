package com.mcp.mcp_pilot.knowledge.adapter.out.outbox;

import com.mcp.mcp_pilot.common.outbox.entity.OutboxEventJpaEntity;
import com.mcp.mcp_pilot.common.outbox.repository.OutboxEventJpaRepository;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgeEventPublishPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KnowledgeOutboxEventAdapter implements KnowledgeEventPublishPort {

    private final OutboxEventJpaRepository outboxEventRepository;

    @Override
    public void publish(String eventType, Long knowledgeId) {
        OutboxEventJpaEntity outboxEvent = OutboxEventJpaEntity.create(
                UUID.randomUUID().toString(),
                eventType,
                String.valueOf(knowledgeId),
                false,
                0
        );
        outboxEventRepository.save(outboxEvent);
    }
}
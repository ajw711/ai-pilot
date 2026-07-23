package com.mcp.mcp_pilot.ops.adapter.out.persistence;

import com.mcp.mcp_pilot.ops.adapter.out.persistence.entity.DeploymentRequestJpaEntity;
import com.mcp.mcp_pilot.ops.adapter.out.persistence.entity.OutboxEventJpaEntity;
import com.mcp.mcp_pilot.ops.adapter.out.persistence.entity.OutboxEventType;
import com.mcp.mcp_pilot.ops.adapter.out.persistence.repository.DeploymentRequestJpaRepository;
import com.mcp.mcp_pilot.ops.adapter.out.persistence.repository.OutboxEventJpaRepository;
import com.mcp.mcp_pilot.ops.application.event.DeploymentRequestedEvent;
import com.mcp.mcp_pilot.ops.exception.DeployPersistenceException;
import com.mcp.mcp_pilot.ops.port.in.dto.DeploymentStatus;
import com.mcp.mcp_pilot.ops.port.out.DeployPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeploymentRequestPersistenceAdapter implements DeployPersistencePort {

    private final DeploymentRequestJpaRepository requestRepository;
    private final OutboxEventJpaRepository outboxRepository;
    private final JsonMapper jsonMapper;

    @Override
    @Transactional
    public void save(DeploymentRequestedEvent event) {
        log.info("[PersistenceAdapter] DB 트랜잭션 저장 시작. TrackingID: {}", event.trackingId());

        try {
            // event로부터 JPA Entity 생성
            DeploymentRequestJpaEntity requestJpaEntity = toEntity(event);
            requestRepository.save(requestJpaEntity);

            // event 자체를 직렬화
            String rawJsonPayload = jsonMapper.writeValueAsString(event);
            String eventId = "EVENT-" + UUID.randomUUID().toString().toUpperCase();

            OutboxEventJpaEntity outboxEntity = OutboxEventJpaEntity.create(
                    eventId,
                    OutboxEventType.DEPLOYMENT_REQUESTED.name(),
                    rawJsonPayload,
                    false,
                    0
            );
            outboxRepository.save(outboxEntity);
        } catch (Exception e) {
            log.error("[PersistenceAdapter] 저장 실패. TrackingID: {}", event.trackingId(), e);
            throw new DeployPersistenceException(e);
        }
        log.info("[PersistenceAdapter] DB 트랜잭션 저장 완료. TrackingID: {}", event.trackingId());
    }

    private DeploymentRequestJpaEntity toEntity(DeploymentRequestedEvent event) {
        return DeploymentRequestJpaEntity.create(
                event.trackingId(),
                event.appName(),
                event.image(),
                event.tag(),
                event.replicas(),
                event.namespace(),
                DeploymentStatus.REQUESTED,
                null
        );
    }
}

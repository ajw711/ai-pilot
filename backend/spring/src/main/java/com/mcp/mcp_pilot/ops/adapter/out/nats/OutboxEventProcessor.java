package com.mcp.mcp_pilot.ops.adapter.out.nats;

import com.mcp.mcp_pilot.ops.adapter.out.persistence.entity.OutboxEventJpaEntity;
import com.mcp.mcp_pilot.ops.adapter.out.persistence.repository.OutboxEventJpaRepository;
import com.mcp.mcp_pilot.ops.exception.OutboxEventNotFoundException;
import com.mcp.mcp_pilot.ops.port.out.DeployPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventProcessor {

    private final OutboxEventJpaRepository outboxEventRepository;
    private final DeployPort deployPort;

    /**
     * 식별자(ID)만 받아 새 트랜잭션 컨텍스트 내부에서 엔티티를 다시 조회(Managed)하여 처리합니다.
     * 이를 통해 JPA의 Dirty Checking과 변경 감지 Flush가 100% 보장됩니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processEvent(Long eventId) {
        log.info("[OutboxEventProcessor] 새 트랜잭션 내부 조회 시작. EventPK: {}", eventId);

        // 새 트랜잭션의 영속성 컨텍스트에 편입(Managed)
        OutboxEventJpaEntity event = outboxEventRepository.findById(eventId)
                .orElseThrow(OutboxEventNotFoundException::new);
        try {
            log.info("[OutboxEventProcessor] NATS 전송 시도. EventID: {}", event.getEventId());

            deployPort.publish(event.getPayload());

            // Managed 상태이므로 트랜잭션이 끝날 때 Dirty Checking되어 DB에 UPDATE commit 됨
            event.markPublished();
            log.info("[OutboxEventProcessor] NATS 전송 성공. EventID: {}", event.getEventId());

        } catch (Exception e) {
            // Managed 상태이므로 트랜잭션이 끝날 때 Dirty Checking되어 DB에 UPDATE commit 됨
            event.incrementRetry();
            log.error("[OutboxEventProcessor] NATS 전송 실패. EventID: {}, 누적 Retry: {}",
                    event.getEventId(), event.getRetryCount(), e);
        }
    }
}

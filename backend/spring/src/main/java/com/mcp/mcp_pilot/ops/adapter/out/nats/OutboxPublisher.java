package com.mcp.mcp_pilot.ops.adapter.out.nats;

import com.mcp.mcp_pilot.ops.adapter.out.persistence.entity.OutboxEventJpaEntity;
import com.mcp.mcp_pilot.ops.adapter.out.persistence.repository.OutboxEventJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventJpaRepository outboxEventRepository;
    private final OutboxEventProcessor outboxEventProcessor; // 헬퍼 컴포넌트 주입

    private static final int MAX_RETRY_COUNT = 5;

    // 스케줄러 자체에는 @Transactional을 붙이지 않아 트랜잭션 범위를 이벤트 건별로 분리.
    @Scheduled(fixedDelay = 5000)
    public void publishOutboxEvents() {
        // 비트랜잭셔널 상태에서 비영속(Detached) 리스트 조회
        List<OutboxEventJpaEntity> unpublishedEvents =
                outboxEventRepository.findByPublishedFalseAndRetryCountLessThan(MAX_RETRY_COUNT);

        if (unpublishedEvents.isEmpty()) {
            return;
        }

        log.info("[OutboxPublisher] 미발행 이벤트 감지. 처리 대상: {}건", unpublishedEvents.size());

        for (OutboxEventJpaEntity event : unpublishedEvents) {
            // 엔티티 객체가 아닌 고유 식별자(PK)만 안전하게 넘김
            outboxEventProcessor.processEvent(event.getId());
        }
    }
}
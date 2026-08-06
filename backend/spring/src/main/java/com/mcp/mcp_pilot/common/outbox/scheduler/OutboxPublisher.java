package com.mcp.mcp_pilot.common.outbox.scheduler;

import com.mcp.mcp_pilot.common.outbox.entity.OutboxEventJpaEntity;
import com.mcp.mcp_pilot.common.outbox.repository.OutboxEventJpaRepository;
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
    private final OutboxEventProcessor outboxEventProcessor;

    private static final int MAX_RETRY_COUNT = 5;

    @Scheduled(fixedDelay = 5000)
    public void publishOutboxEvents() {
        List<OutboxEventJpaEntity> unpublishedEvents =
                outboxEventRepository.findPendingEvents(MAX_RETRY_COUNT);

        if (unpublishedEvents.isEmpty()) {
            return;
        }

        log.info("[OutboxPublisher] 미발행 이벤트 감지. 처리 대상: {}건", unpublishedEvents.size());

        for (OutboxEventJpaEntity event : unpublishedEvents) {
            outboxEventProcessor.processEvent(event.getId());
        }
    }
}

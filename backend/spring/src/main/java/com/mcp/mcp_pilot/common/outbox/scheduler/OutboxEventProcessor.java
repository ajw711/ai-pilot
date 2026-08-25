package com.mcp.mcp_pilot.common.outbox.scheduler;

import com.mcp.mcp_pilot.common.config.NatsConnectionHolder;
import com.mcp.mcp_pilot.common.outbox.entity.OutboxEventJpaEntity;
import com.mcp.mcp_pilot.common.outbox.repository.OutboxEventJpaRepository;
import io.nats.client.JetStream;
import io.nats.client.PublishOptions;
import io.nats.client.api.PublishAck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventProcessor {

    private final OutboxEventJpaRepository outboxEventRepository;
    private final NatsConnectionHolder connectionHolder;
    private static final int MAX_RETRY_COUNT = 5;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processEvent(Long eventId) {
        OutboxEventJpaEntity event = outboxEventRepository
                .findByIdForUpdateSkipLocked(eventId)
                .orElse(null);

        if (event == null) {
            return;
        }

        try {
            log.info("[OutboxEventProcessor] JetStream 전송 시도. EventID: {}, Subject: {}", event.getEventId(), event.getEventType());

            JetStream js = connectionHolder.getJetStream();

            // https://docs.nats.io/learn/jetstream/your-first-stream
            PublishOptions options = PublishOptions.builder()
                    .messageId(event.getEventId())
                    .build();

            PublishAck ack = js.publish(
                    event.getEventType(),
                    event.getPayload().getBytes(StandardCharsets.UTF_8),
                    options
            );

            event.markPublished();
            log.info("[OutboxEventProcessor] JetStream 전송 성공. Seq: {}", ack.getSeqno());

        } catch (Exception e) {
            event.incrementRetry();
            if (event.getRetryCount() >= MAX_RETRY_COUNT) {
                event.markFailed();
                log.error("CRITICAL: [OutboxDLQ] 최대 재시도 횟수(5회) 초과! EventID: {}, Subject: {}", event.getEventId(), event.getEventType(), e);
            } else {
                log.error("[OutboxEventProcessor] JetStream 전송 실패. EventID: {}, Retry: {}", event.getEventId(), event.getRetryCount(), e);
            }
        }
    }
}

package com.mcp.mcp_pilot.knowledge.adapter.out.nats;


import com.mcp.mcp_pilot.common.config.NatsConnectionHolder;
import com.mcp.mcp_pilot.common.config.event.NatsConnectedEvent;
import com.mcp.mcp_pilot.knowledge.application.service.KnowledgeStatusAggregator;
import io.nats.client.*;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeStatusAggregatorListener {

    private final NatsConnectionHolder connectionHolder;
    private final KnowledgeStatusAggregator statusAggregator;
    private JetStreamSubscription subscription;

    @EventListener(NatsConnectedEvent.class)
    public void subscribe() {
        // 재연결 시 기존 클라이언트 구독 객체가 남아있다면 메모리 누수/중복방지를 위해 사전 정리
        if (subscription != null) {
            try {
                subscription.unsubscribe();
                log.info("[KnowledgeStatusAggregatorListener] 기존 구독 정리 완료 (재연결 대비)");
            } catch (Exception e) {
                log.warn("[KnowledgeStatusAggregatorListener] 기존 구독 정리 중 예외 (무시): {}", e.getMessage());
            }
            subscription = null;
        }
        // NATS JetStream Durable 구독 수행
        try {
            JetStream js = connectionHolder.getJetStream();
            PushSubscribeOptions options = PushSubscribeOptions.builder()
                    .stream("KNOWLEDGE_EVENTS")               // 대상 스트림 명시
                    .durable("knowledge-status-aggregator")  // Durable Consumer 오프셋 보존 이름
                    .build();
            Dispatcher dispatcher = connectionHolder.getConnection().createDispatcher();

            subscription = js.subscribe(
                    "knowledge.>",
                    dispatcher,
                    this::handleMessage,
                    false,
                    options
            );
            log.info("[KnowledgeStatusAggregatorListener] JetStream 구독 완료: knowledge.>");
        } catch (Exception e) {
            log.warn("[KnowledgeStatusAggregatorListener] JetStream 구독 대기 (NATS 연결 대기 중)");
        }
    }

    private void handleMessage(Message msg) {
        try {
            String knowledgeIdStr = new String(msg.getData(), StandardCharsets.UTF_8);
            Long knowledgeId = Long.parseLong(knowledgeIdStr.trim());

            log.info("[StatusAggregatorListener] 이벤트 수신 - Subject: {}, KnowledgeID: {}", msg.getSubject(), knowledgeId);

            statusAggregator.recompute(knowledgeId);

            msg.ack();
        } catch (Exception e) {
            log.error("[StatusAggregatorListener] 이벤트 처리 실패 - 재전송 대기", e);
            msg.nak();
        }
    }

    @PreDestroy
    public void unsubscribe() {
        if (subscription != null) {
            try {
                subscription.unsubscribe();
            } catch (Exception ignored) {
            }
        }
    }

}

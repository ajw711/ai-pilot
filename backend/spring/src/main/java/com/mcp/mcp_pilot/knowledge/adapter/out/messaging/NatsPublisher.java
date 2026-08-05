package com.mcp.mcp_pilot.knowledge.adapter.out.messaging;

import com.mcp.mcp_pilot.common.config.NatsConnectionHolder;
import io.nats.client.Connection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class NatsPublisher {

    private final NatsConnectionHolder connectionHolder;
    private final JsonMapper jsonMapper;

    public void publish(String subject, Object payload) {
        Connection natsConnection  = connectionHolder.getConnection();
        if (natsConnection  == null || natsConnection .getStatus() != Connection.Status.CONNECTED) {
            log.warn("[NatsPublisher] NATS 미연결 상태 - publish 일시 비활성화.");
            return;
        }

        try {
            byte[] data = jsonMapper.writeValueAsBytes(payload);
            natsConnection.publish(subject, data);
            log.info("[NATS Publisher] Published message to subject: {}", subject);
        } catch (Exception e) {
            log.error("[NATS Publisher] Failed to publish message to subject: {}", subject, e);
        }
    }

}

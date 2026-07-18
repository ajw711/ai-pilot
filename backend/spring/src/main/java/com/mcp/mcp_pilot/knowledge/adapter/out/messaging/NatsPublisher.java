package com.mcp.mcp_pilot.knowledge.adapter.out.messaging;

import io.nats.client.Connection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class NatsPublisher {

    private final Connection connection;
    private final JsonMapper jsonMapper;

    public void publish(String subject, Object payload) {
        try {
            byte[] data = jsonMapper.writeValueAsBytes(payload);
            connection.publish(subject, data);
            log.info("[NATS Publisher] Published message to subject: {}", subject);
        } catch (Exception e) {
            log.error("[NATS Publisher] Failed to publish message to subject: {}", subject, e);
        }
    }

}

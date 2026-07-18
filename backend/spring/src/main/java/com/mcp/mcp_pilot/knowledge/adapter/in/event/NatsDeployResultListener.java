package com.mcp.mcp_pilot.knowledge.adapter.in.event;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class NatsDeployResultListener {

    private final Connection natsConnection;

    @PostConstruct
    public void initSubscription() {
        log.info("[NATS Listener] Registering subscription to deploy.result");
        try {
            Dispatcher dispatcher = natsConnection.createDispatcher(msg -> {
                String payload = new String(msg.getData(), StandardCharsets.UTF_8);
                log.info("[NATS Listener] Received deploy.result message: {}", payload);
            });
            dispatcher.subscribe("deploy.result");
        } catch (Exception e) {
            log.error("[NATS Listener] Failed to subscribe to deploy.result", e);
        }
    }
}

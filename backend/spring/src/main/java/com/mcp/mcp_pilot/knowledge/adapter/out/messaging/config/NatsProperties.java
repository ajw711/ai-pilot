package com.mcp.mcp_pilot.knowledge.adapter.out.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.nats")
public record NatsProperties(String url) {
}

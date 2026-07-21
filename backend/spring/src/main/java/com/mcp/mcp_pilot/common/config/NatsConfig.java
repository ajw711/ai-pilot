package com.mcp.mcp_pilot.common.config;

import io.nats.client.Connection;
import io.nats.client.Nats;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NatsConfig {

    @Value("${spring.nats.url:nats://localhost:4222}")
    private String natsUrl;

    @Bean
    public Connection natsConnection() throws Exception {
        return Nats.connect(natsUrl);
    }

}

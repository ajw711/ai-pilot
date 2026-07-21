package com.mcp.mcp_pilot.common.config;

import com.mcp.mcp_pilot.knowledge.adapter.out.messaging.config.NatsProperties;
import com.mcp.mcp_pilot.knowledge.adapter.out.notion.config.NotionProperties;
import io.nats.client.Connection;
import io.nats.client.Nats;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NatsProperties.class)
public class NatsConfig {

    @Bean
    public Connection natsConnection(NatsProperties properties) throws Exception {
        return Nats.connect(properties.url());
    }

}

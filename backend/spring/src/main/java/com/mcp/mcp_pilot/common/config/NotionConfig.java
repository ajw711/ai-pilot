package com.mcp.mcp_pilot.common.config;


import com.mcp.mcp_pilot.knowledge.adapter.out.notion.config.NotionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.ApiVersionInserter;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(NotionProperties.class)
public class NotionConfig {

    @Bean(name = "notionRestClient")
    public RestClient notionRestClient(NotionProperties properties) {
        return RestClient.builder()
                .baseUrl("https://api.notion.com")
                .apiVersionInserter(ApiVersionInserter.useHeader("API-Version"))
                .defaultHeader("Authorization", "Bearer " + properties.token())
                .defaultHeader("Notion-Version", properties.version())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}

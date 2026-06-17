package com.mcp.mcp_pilot.common.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class NotionConfig {

    public RestClient notionRestClient(RestClient.Builder builder,
                                       @Value("${notion.api.token}") String token,
                                       @Value("${notion.api.version}") String version) {
        return builder
                .baseUrl("https://api.notion.com")
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Notion-Version", version)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}

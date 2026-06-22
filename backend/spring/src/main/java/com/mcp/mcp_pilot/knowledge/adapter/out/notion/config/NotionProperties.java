package com.mcp.mcp_pilot.knowledge.adapter.out.notion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notion.api")
public record NotionProperties(
        String token,
        String databaseId,
        String version
) {
}

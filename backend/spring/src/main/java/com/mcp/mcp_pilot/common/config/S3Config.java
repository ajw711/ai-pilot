package com.mcp.mcp_pilot.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloudflare.r2")
public record S3Config(
        String accountId,
        String accessKey,
        String secretKey,
        String bucketName
) {
    public String endpoint() {
        return "https://%s.r2.cloudflarestorage.com".formatted(accountId);
    }
}
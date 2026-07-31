package com.mcp.mcp_pilot.ops.application.domain.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;


@Getter
public class SseTicket {

    private final String ticket;
    private final Long userId;
    private final String clientIp;
    private final String userAgent;
    private final Instant expiresAt;

    @Builder
    public SseTicket(
            String ticket,
            Long userId,
            String clientIp,
            String userAgent,
            Instant expiresAt
    ) {
        this.ticket = ticket;
        this.userId = userId;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
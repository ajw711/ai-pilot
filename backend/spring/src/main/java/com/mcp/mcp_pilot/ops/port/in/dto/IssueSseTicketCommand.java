package com.mcp.mcp_pilot.ops.port.in.dto;

public record IssueSseTicketCommand(
        Long userId,
        String clientIp,
        String userAgent
) {
}

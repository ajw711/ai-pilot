package com.mcp.mcp_pilot.user.port.in.dto;

public record LoginResult(
        boolean success,
        String accessToken,
        String refreshToken,
        String message
) {}

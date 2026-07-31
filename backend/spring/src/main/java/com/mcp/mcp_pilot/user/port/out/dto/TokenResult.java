package com.mcp.mcp_pilot.user.port.out.dto;

public record TokenResult(
        String accessToken,
        String refreshToken
) {}

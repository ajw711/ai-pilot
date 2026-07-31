package com.mcp.mcp_pilot.user.port.in.dto;

public record LogoutCommand(
        String refreshToken
) {}

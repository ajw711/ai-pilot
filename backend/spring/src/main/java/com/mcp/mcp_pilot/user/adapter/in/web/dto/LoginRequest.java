package com.mcp.mcp_pilot.user.adapter.in.web.dto;

public record LoginRequest(
        String username,
        String password
) {
}

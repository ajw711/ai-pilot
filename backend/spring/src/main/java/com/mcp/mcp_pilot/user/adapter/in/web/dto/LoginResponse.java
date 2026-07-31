package com.mcp.mcp_pilot.user.adapter.in.web.dto;

public record LoginResponse(
        String accessToken
) {
    public static LoginResponse from(String accessToken) {
        return new LoginResponse(accessToken);
    }
}

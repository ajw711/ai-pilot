package com.mcp.mcp_pilot.user.port.in.dto;

public record LoginCommand(
        String username,
        String password
) {}

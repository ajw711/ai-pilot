package com.mcp.mcp_pilot.dto.chat;

public record ChatResponse(String answer) {

    public static ChatResponse of(String answer) {
        return new ChatResponse(answer);
    }
}

package com.mcp.mcp_pilot.ai.dto;

import com.mcp.mcp_pilot.ai.enums.AIModel;

import java.util.List;

public record AiRequest(
        String message,
        AIModel aiModel,
        List<String> tools
) {

    public static AiRequest of(String message, AIModel aiModel, List<String> tools) {
        return new AiRequest(message, aiModel, tools);
    }
}

package com.mcp.mcp_pilot.ai.dto;

import com.mcp.mcp_pilot.ai.enums.AIModel;
import com.mcp.mcp_pilot.common.enums.ToolType;

import java.util.List;

/**
 * AI orchestration 내부 DTO
 * @param message
 * @param model
 * @param tools
 */
public record AiRequest(
        Long userId,
        String message,
        AIModel model,
        List<ToolType> tools
) {

    public static AiRequest of(Long userId, String message, AIModel model, List<ToolType> tools) {
        return new AiRequest(userId, message, model, tools);
        
    }
}

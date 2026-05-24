package com.mcp.mcp_pilot.ai.dto;

/**
 * 외부 API 입력 DTO
 * @param answer 질문
 */
public record ChatResponse(String answer) {

    public static ChatResponse of(String answer) {
        return new ChatResponse(answer);
    }
}

package com.mcp.mcp_pilot.common.dto;

import com.mcp.mcp_pilot.common.enums.ToolStatus;

/**
 * MCP Tool 실행 결과 공통 응답 객체
 *
 * @param status Tool 실행 상태
 * @param message Tool 실행 결과 메시지
 * @param data Tool 실행 결과 데이터
 * @param <T> 반환 데이터 타입
 */
public record ToolResponse<T>(
        ToolStatus status,
        String message,
        T data
) {

    public static <T> ToolResponse<T> of(ToolStatus status,String message, T data) {
        return new ToolResponse<>(status, message, data);
    }

    public static <T> ToolResponse<T> success(String message, T data) {
        return new ToolResponse<>(ToolStatus.SUCCESS, message, data);
    }

    public static <T> ToolResponse<T> fail(String message) {
        return new ToolResponse<>(ToolStatus.FAIL, message, null);
    }
}

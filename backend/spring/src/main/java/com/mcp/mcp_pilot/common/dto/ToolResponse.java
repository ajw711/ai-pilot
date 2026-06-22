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
    // 성공 여부를 확인하기 위한 메서드
    public boolean isSuccess() {
        return this.status != null && ToolStatus.SUCCESS.name().equals(this.status.name());
    }

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

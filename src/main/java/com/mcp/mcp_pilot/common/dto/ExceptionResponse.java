package com.mcp.mcp_pilot.common.dto;

public record ExceptionResponse(
        String code,
        String message
) {
    public static ExceptionResponse of(String code, String message) {
        return new ExceptionResponse(code, message);
    }
}

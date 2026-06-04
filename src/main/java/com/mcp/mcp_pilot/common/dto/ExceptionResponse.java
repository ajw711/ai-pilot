package com.mcp.mcp_pilot.common.dto;

import com.mcp.mcp_pilot.common.exception.ErrorCode;

public record ExceptionResponse(
        String code,
        String message
) {

    public static ExceptionResponse of(ErrorCode errorCode) {
        return new ExceptionResponse(
                errorCode.getCode(),
                errorCode.getMessage()
        );
    }
}
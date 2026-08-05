package com.mcp.mcp_pilot.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mcp.mcp_pilot.common.exception.ErrorCode;

import org.springframework.http.HttpStatus;

public record ApiResponse<T>(
        @JsonIgnore
        HttpStatus httpStatus,
        T data,
        ExceptionResponse error
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                HttpStatus.OK,
                data,
                null
        );
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return new ApiResponse<>(
                errorCode.getStatus(),
                null,
                ExceptionResponse.of(errorCode)
        );
    }
}
package com.mcp.mcp_pilot.common.dto;

import com.mcp.mcp_pilot.common.exception.ErrorCode;
import net.minidev.json.annotate.JsonIgnore;
import org.springframework.http.HttpStatus;

import javax.annotation.Nullable;

public record ApiResponse<T>(
        @JsonIgnore
        HttpStatus httpStatus,
        boolean success,
        T data,
        ExceptionResponse error
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                HttpStatus.OK,
                true,
                data,
                null
        );
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return new ApiResponse<>(
                errorCode.getStatus(),
                false,
                null,
                ExceptionResponse.of(errorCode)
        );
    }
}
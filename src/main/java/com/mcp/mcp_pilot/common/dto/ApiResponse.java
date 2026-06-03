package com.mcp.mcp_pilot.common.dto;

import net.minidev.json.annotate.JsonIgnore;
import org.springframework.http.HttpStatus;

import javax.annotation.Nullable;

public record ApiResponse<T>(
        @JsonIgnore
        HttpStatus httpStatus,
        boolean success,
        @Nullable T data,
        @Nullable ExceptionResponse error
) {

    public static <T> ApiResponse<T> success(@Nullable final T data) {
        return new ApiResponse<>(HttpStatus.OK, true, data, null);
    }

    public static <T> ApiResponse<T> fail(HttpStatus status, ExceptionResponse error) {
        return new ApiResponse<>(status, false, null, error);
    }

}

package com.mcp.mcp_pilot.common.exception;

import com.mcp.mcp_pilot.common.dto.ApiResponse;
import com.mcp.mcp_pilot.common.dto.ExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleAllException(Exception e) {
        log.error("서버 오류: {}", e.getMessage());
        ApiResponse<?> response = ApiResponse.fail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ExceptionResponse.of("SERVER_ERROR", "시스템 장애가 발생했습니다.")
        );
        return ResponseEntity.status(response.httpStatus()).body(response);
    }
}

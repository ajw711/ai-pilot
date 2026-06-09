package com.mcp.mcp_pilot.common.exception;

import com.mcp.mcp_pilot.common.dto.ApiResponse;
import com.mcp.mcp_pilot.common.dto.ExceptionResponse;
import com.mcp.mcp_pilot.common.dto.ToolResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bean Validation (@Valid, @Validated) 실패 처리
     * MCP Tool 호출 시 데이터가 누락되면 AI에게 에러 메시지를 전달합니다.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleValidationException(ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations().iterator().next().getMessage();
        log.warn("데이터 검증 실패: {}", errorMessage);

        // AI가 이해할 수 있게 ToolResponse로 포장하여 반환
        return ResponseEntity.badRequest()
                .body(ToolResponse.fail("검증 실패: " + errorMessage));
    }


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException e) {
        log.warn("비즈니스 로직 오류: {} (Code: {})", e.getMessage(), e.getErrorCode());
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.fail(e.getErrorCode()));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<?>> handleDatabaseException(DataAccessException e) {
        log.error("데이터베이스 에러 발생: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(
                        ErrorCode.INTERNAL_SERVER_ERROR
                ));
    }

    /**
     *일반적인 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleAllException(Exception e) {
        log.error("서버 오류", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(
                        ErrorCode.INTERNAL_SERVER_ERROR
                ));
    }
}

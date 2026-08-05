package com.mcp.mcp_pilot.common.exception;

import com.mcp.mcp_pilot.common.dto.ApiResponse;
import com.mcp.mcp_pilot.common.dto.ToolResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bean Validation (@Valid, @Validated) 실패 처리
     * MCP Tool 호출 시 데이터가 누락되면 AI에게 에러 메시지를 전달.
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
    public Object handleBusinessException(BusinessException e, HttpServletRequest request) {
        if (isSseRequest(request)) {
            return buildSseErrorResponse(e.getMessage());
        }
        log.warn("비즈니스 로직 오류: {} (Code: {})", e.getMessage(), e.getErrorCode());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.fail(e.getErrorCode()));
    }

    @ExceptionHandler(DataAccessException.class)
    public Object handleDatabaseException(DataAccessException e, HttpServletRequest request) {
        if (isSseRequest(request)) {
            return buildSseErrorResponse("데이터베이스 오류가 발생했습니다.");
        }
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
    public Object handleAllException(Exception e, HttpServletRequest request) {
        if (isSseRequest(request)) {
            return buildSseErrorResponse("서버 오류가 발생했습니다.");
        }
        log.error("서버 오류", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(
                        ErrorCode.INTERNAL_SERVER_ERROR
                ));
    }

    /**
     *이미 끊어진 비동기 클라이언트 연결(SSE)에 대한 예외 및 타임아웃은 로그만 남기고 조용히 넘김
     */
    @ExceptionHandler({AsyncRequestNotUsableException.class, AsyncRequestTimeoutException.class})
    public void handleAsyncExceptions(Exception e) {
        log.info("[GlobalExceptionHandler] 비동기 SSE 세션 정리 완료: {}", e.getClass().getSimpleName());
    }

    private boolean isSseRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String uri = request.getRequestURI();
        return (accept != null && accept.contains("text/event-stream"))
                || (uri != null && (uri.contains("/stream") || uri.contains("/notifications")));
    }

    private SseEmitter buildSseErrorResponse(String message) {
        SseEmitter emitter =
                new SseEmitter(0L);
        try {
            emitter.send(SseEmitter.event()
                    .name("error").data(message));
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }
}

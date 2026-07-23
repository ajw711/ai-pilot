package com.mcp.mcp_pilot.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    /**
     * Cxxx = Common
     * Kxxx = Knowledge
     * Nxxx = Notion
     * Vxxx = Vector
     * Axxx = AI
     * Sxxx = Search
     * Oxxx = OPS
     */

    // 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "올바르지 않은 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류입니다."),

    // AI/벡터 관련 (계층화)
    AI_EMBEDDING_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "A001", "AI 의미 추출(Embedding)에 실패했습니다."),
    AI_VECTOR_STORAGE_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "A002", "벡터 데이터 저장에 실패했습니다."),

    // Knowledge 관련
    KNOWLEDGE_NOT_FOUND(HttpStatus.NOT_FOUND, "K001", "지식 데이터를 찾을 수 없습니다."),
    KNOWLEDGE_PUBLISH_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR,"K002","지식 발행에 실패했습니다."),
    INVALID_KNOWLEDGE_STATUS(HttpStatus.BAD_REQUEST, "K003", "승인할 수 없는 상태의 지식 데이터입니다."),

    // Ops 관련
    DEPLOY_PUBLISH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "OPS001", "배포 요청 전송에 실패했습니다."),
    DEPLOY_PERSISTENCE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "OPS002", "배포 요청 데이터 저장에 실패했습니다."),
    OUTBOX_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "OPS003", "존재하지 않는 아웃박스 이벤트입니다.");

    private final HttpStatus status; // HTTP 상태 코드
    private final String code; // 에러 코드
    private final String message; // 사용자/AI에게 보여줄 메시지
}

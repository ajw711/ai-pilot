package com.mcp.mcp_pilot.ops.port.in.dto;

public enum DeployResultStatus {
    ACCEPTED, // 요청 접수 성공 (비동기 처리 시작됨)
    REJECTED, // 입력값 오류 또는 사내 보안 정책 위반으로 접수 거절
    FAILED    // 요청을 처리(예: DB 저장 등)하는 도중 에러 발생
}

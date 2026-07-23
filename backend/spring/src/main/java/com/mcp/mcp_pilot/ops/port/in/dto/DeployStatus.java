package com.mcp.mcp_pilot.ops.port.in.dto;


public enum DeployStatus {
    ACCEPTED, // 정상 접수
    REJECTED, // 정책 위반 등으로 거럴
    FAILED // 인프라 전송 실패 등 오류 발생
}

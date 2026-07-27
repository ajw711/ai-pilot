package com.mcp.mcp_pilot.ops.adapter.in.web.dto;

public enum EventType {
    TOKEN,        // 실시간 생성 텍스트 조각
    STATUS,       // 툴 실행 중 등의 진행 상태 메시지
    TOOL_START,   // 툴 호출 시작
    TOOL_END,     // 툴 호출 완료 (결과값 전달)
    ERROR,        // 예외 발생
    COMPLETE      // 스트림 완전 종료
}

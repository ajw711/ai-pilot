package com.mcp.mcp_pilot.ai.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VectorTargetType {
    KNOWLEDGE("지식 데이터"),
    K8S_LOG("쿠버네티스 로그"),
    CHAT_HISTORY("대화 이력");

    private final String description;
}

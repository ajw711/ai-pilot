package com.mcp.mcp_pilot.knowledge.domain.vo;

/**
 * 지식 검증 과정에서 발견된 구체적인 문제점 또는 제안 사항
 */
public record Issue(
    Severity severity,
    String targetText,
    String message
) {
}

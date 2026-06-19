package com.mcp.mcp_pilot.knowledge.domain.vo;

/**
 * 모호한 문장과 이에 대한 개선 추천 가이드를 보관
 * @param text
 * @param suggestion
 */
public record AmbiguousExpression(
    String text,
    String suggestion
) {
}

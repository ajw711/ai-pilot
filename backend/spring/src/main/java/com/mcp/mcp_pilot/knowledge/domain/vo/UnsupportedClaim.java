package com.mcp.mcp_pilot.knowledge.domain.vo;

/**
 * 기술적 근거가 부족한 단정적 주장과 이유를 보관
 * @param text
 * @param reason
 */
public record UnsupportedClaim(
    String text,
    String reason
) {
}

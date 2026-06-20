package com.mcp.mcp_pilot.knowledge.domain.vo;

import java.util.List;

/**
 * 지식 검증 결과를 종합한 리포트 DTO
 */
public record VerificationReport(
    List<Issue> issues
) {
    public static VerificationReport empty() {
        return new VerificationReport(List.of());
    }
}

package com.mcp.mcp_pilot.knowledge.domain.vo;

import java.util.List;

/**
 * 감점 산출 로직을 거쳐 최종 판정된 신뢰도 수치를 가진 결과
 * @param confidenceScore
 * @param factIssues
 * @param ambiguities
 * @param unsupportedClaims
 * @param reviewRequired
 */
public record VerificationResult(
    int confidenceScore,
    List<FactIssue> factIssues,
    List<AmbiguousExpression> ambiguities,
    List<UnsupportedClaim> unsupportedClaims,
    boolean reviewRequired
) {
}

package com.mcp.mcp_pilot.knowledge.adapter.out.ai.dto;

import com.mcp.mcp_pilot.knowledge.domain.vo.AmbiguousExpression;
import com.mcp.mcp_pilot.knowledge.domain.vo.FactIssue;
import com.mcp.mcp_pilot.knowledge.domain.vo.UnsupportedClaim;

import java.util.List;

/**
 * AI가 생성한 JSON 형식을 Spring AI Structured Output으로 직접 바인딩 받기 위한 응답 래퍼
 * (LLM)이 생성하는 답변을 단순한 텍스트(Plain Text)가 아니라
 * JSON, XML, YAML 등 개발자가 지정한 명확한 데이터 구조(Schema)에 맞춰 출력하도록 강제하는 기술
 * @param factIssues
 * @param ambiguities
 * @param unsupportedClaims
 */
public record VerificationResponse(
    List<FactIssue> factIssues,
    List<AmbiguousExpression> ambiguities,
    List<UnsupportedClaim> unsupportedClaims
) {
}

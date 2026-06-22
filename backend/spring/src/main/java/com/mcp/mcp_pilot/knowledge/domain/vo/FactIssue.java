package com.mcp.mcp_pilot.knowledge.domain.vo;

public record FactIssue(
    String originalText,
    String reason,
    Severity severity
) {
}

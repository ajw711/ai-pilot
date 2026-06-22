package com.mcp.mcp_pilot.knowledge.port.in.dto;

public record ApproveKnowledgeCommand(
        Long knowledgeId,
        String finalFormattedContent
) {
}

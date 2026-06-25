package com.mcp.mcp_pilot.knowledge.port.in.dto;

import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;

public record KnowledgeSummary(
        Long id,
        String title,
        KnowledgeStatus status
) {

    public static KnowledgeSummary of(Long knowledgeId, String title, KnowledgeStatus status) {
        return new KnowledgeSummary(
                knowledgeId,
                title,
                status
        );
    }
}

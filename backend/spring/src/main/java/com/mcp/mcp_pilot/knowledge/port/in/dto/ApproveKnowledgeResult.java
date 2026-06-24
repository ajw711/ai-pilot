package com.mcp.mcp_pilot.knowledge.port.in.dto;

import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;

public record ApproveKnowledgeResult(
        Long knowledgeId,
        KnowledgeStatus status
) {
    public static ApproveKnowledgeResult of(Long knowledgeId, KnowledgeStatus status) {
        return new ApproveKnowledgeResult(
                knowledgeId,
                status
        );
    }
}

package com.mcp.mcp_pilot.knowledge.port.in.dto;

import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;

public record SaveKnowledgeResult(
        Long knowledgeId,
        KnowledgeStatus status
) {

    public static SaveKnowledgeResult of(Long knowledgeId, KnowledgeStatus status) {
        return new SaveKnowledgeResult(
                knowledgeId,
                status
        );
    }
}

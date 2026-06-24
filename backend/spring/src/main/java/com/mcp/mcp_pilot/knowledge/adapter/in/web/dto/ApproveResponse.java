package com.mcp.mcp_pilot.knowledge.adapter.in.web.dto;

import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;
import com.mcp.mcp_pilot.knowledge.port.in.dto.ApproveKnowledgeResult;
import com.mcp.mcp_pilot.knowledge.port.in.dto.SaveKnowledgeResult;

public record ApproveResponse(
        Long knowledgeId,
        KnowledgeStatus status
) {
    public static ApproveResponse from(ApproveKnowledgeResult result) {
        return new ApproveResponse(
                result.knowledgeId(),
                result.status()
        );
    }
}

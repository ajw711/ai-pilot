package com.mcp.mcp_pilot.knowledge.adapter.in.web.dto;

import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;
import com.mcp.mcp_pilot.knowledge.port.in.DeleteKnowledgeUseCase;
import com.mcp.mcp_pilot.knowledge.port.in.dto.DeleteKnowledgeResult;

import java.time.LocalDateTime;

public record DeleteKnowledgeResponse(
        Long knowledgeId,
        KnowledgeStatus status,
        LocalDateTime deletedAt
) {
    public static DeleteKnowledgeResponse from(DeleteKnowledgeResult result) {
        return new DeleteKnowledgeResponse(
                result.knowledgeId(),
                result.status(),
                result.deleteAt()
        );
    }
}

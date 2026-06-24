package com.mcp.mcp_pilot.knowledge.port.in.dto;

import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;

import java.time.LocalDateTime;

public record DeleteKnowledgeResult(
        Long knowledgeId,
        KnowledgeStatus status,
        LocalDateTime deleteAt
) {
    public static DeleteKnowledgeResult of(Long knowledgeId, KnowledgeStatus status, LocalDateTime now) {
        return new DeleteKnowledgeResult(
                knowledgeId,
                status,
                now
        );
    }
}

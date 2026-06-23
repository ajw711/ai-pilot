package com.mcp.mcp_pilot.knowledge.port.in.dto;

import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;

public record KnowledgeSummary(
        Long id,
        String title,
        KnowledgeStatus status
) {

    public static KnowledgeSummary from(KnowledgeLog log) {
        return new KnowledgeSummary(
                log.getId(),
                log.getTitle(),
                log.getStatus()
        );
    }
}

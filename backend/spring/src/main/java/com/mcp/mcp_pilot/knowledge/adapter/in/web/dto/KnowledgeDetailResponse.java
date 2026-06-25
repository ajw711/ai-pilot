package com.mcp.mcp_pilot.knowledge.adapter.in.web.dto;

import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;
import com.mcp.mcp_pilot.knowledge.port.in.dto.KnowledgeDetailResult;

public record KnowledgeDetailResponse(
        Long id,
        String title,
        String rawContent,
        String formattedContent,
        Integer verificationScore,
        String verificationReport,
        KnowledgeStatus status
) {
    public static KnowledgeDetailResponse from(KnowledgeDetailResult result) {
        return new KnowledgeDetailResponse(
                result.id(),
                result.title(),
                result.rawContent(),
                result.formattedContent(),
                result.verificationScore(),
                result.verificationReport(),
                result.status()
        );
    }
}

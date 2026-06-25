package com.mcp.mcp_pilot.knowledge.port.in.dto;

import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;

public record KnowledgeDetailResult(
        Long id,
        String title,
        String rawContent,
        String formattedContent,
        Integer verificationScore,
        String verificationReport,
        KnowledgeStatus status
) {

    public static KnowledgeDetailResult of(
            Long id,
            String title,
            String rawContent,
            String formattedContent,
            Integer verificationScore,
            String verificationReport,
            KnowledgeStatus status
    ) {
        return new KnowledgeDetailResult(
                id,
                title,
                rawContent,
                formattedContent,
                verificationScore,
                verificationReport,
                status
        );
    }


}

package com.mcp.mcp_pilot.knowledge.dto;

import java.util.List;

public record KnowledgeRequest(
        String rawContent,
        String summarizedContent,
        List<String> tags,
        List<String> sourceUrls
) {
}

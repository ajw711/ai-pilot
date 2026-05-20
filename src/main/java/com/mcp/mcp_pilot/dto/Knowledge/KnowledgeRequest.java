package com.mcp.mcp_pilot.dto.Knowledge;

import java.util.List;

public record KnowledgeRequest(
        String rawContent,
        String summarizedContent,
        List<String> tags,
        List<String> sourceUrls
) {
}

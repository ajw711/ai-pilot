package com.mcp.mcp_pilot.knowledge.adapter.in.web.dto;

public record SearchResponse(String content) {
    public static SearchResponse of(String content) {
        return new SearchResponse(content);
    }
}

package com.mcp.mcp_pilot.knowledge.adapter.in.web.dto;

public record SearchResponse(String content) {
    public static SearchResponse from(String content) {
        return new SearchResponse(content);
    }
}

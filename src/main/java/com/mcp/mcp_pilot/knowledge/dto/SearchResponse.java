package com.mcp.mcp_pilot.knowledge.dto;

public record SearchResponse(
    String result
) {
    public static SearchResponse of(String result) {
        return new SearchResponse(result);
    }
}

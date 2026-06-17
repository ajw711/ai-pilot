package com.mcp.mcp_pilot.knowledge.adapter.out.notion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NotionPageResponse(
        String id,
        String url,
        @JsonProperty("created_time")
        String createTime
) {
}

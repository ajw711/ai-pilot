package com.mcp.mcp_pilot.knowledge.adapter.out.notion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Parent(
        @JsonProperty("database_id")
        String databaseId
) {
}

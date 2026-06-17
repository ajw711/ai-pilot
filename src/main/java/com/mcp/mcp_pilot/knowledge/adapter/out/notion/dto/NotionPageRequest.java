package com.mcp.mcp_pilot.knowledge.adapter.out.notion.dto;

import java.util.Map;

public record NotionPageRequest(
        Parent parent,
        Map<String, Object> properties
) {
}

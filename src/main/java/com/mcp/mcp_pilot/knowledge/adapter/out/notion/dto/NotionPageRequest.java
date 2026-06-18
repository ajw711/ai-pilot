package com.mcp.mcp_pilot.knowledge.adapter.out.notion.dto;

import java.util.List;
import java.util.Map;

public record NotionPageRequest(
        Parent parent,
        Map<String, Object> properties,
        List<Map<String, Object>> children
) {
    public NotionPageRequest(Parent parent, Map<String, Object> properties) {
        this(parent, properties, List.of());
    }
}

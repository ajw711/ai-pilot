package com.mcp.mcp_pilot.knowledge.adapter.out.notion.dto;

import java.util.List;
import java.util.Map;

public record NotionPublishPayload(
        NotionPageRequest pageRequest,
        List<List<Map<String, Object>>> remainingChunks
) {
}

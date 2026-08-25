package com.mcp.mcp_pilot.ai.vector.dto;

import java.util.Map;

public record EmbeddedChunk(
        int chunkIndex,
        String content,
        Map<String, Object> metadata,
        float[] vector
) {
}

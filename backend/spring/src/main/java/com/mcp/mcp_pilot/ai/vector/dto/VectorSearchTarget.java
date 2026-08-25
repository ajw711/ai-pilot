package com.mcp.mcp_pilot.ai.vector.dto;

import java.util.Map;

public record VectorSearchTarget(
        Long sourceId,
        Long chunkId,
        String content,
        Map<String, Object> metadata,
        float[] embeddingVector
) {}
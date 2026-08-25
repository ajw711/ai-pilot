package com.mcp.mcp_pilot.ai.vector.port;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.vector.dto.RawChunk;

import java.util.List;

public interface VectorIndexingUseCase {
    void indexChunks(VectorTargetType sourceType, Long sourceId, List<RawChunk> chunks);
    void deleteIndex(VectorTargetType sourceType, Long sourceId);
}

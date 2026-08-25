package com.mcp.mcp_pilot.ai.vector.port;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.vector.dto.EmbeddedChunk;

import java.util.List;

public interface VectorStorePersistencePort {
    void saveChunks(VectorTargetType sourceType, Long sourceId, List<EmbeddedChunk> chunks);
    void deleteIndex(VectorTargetType sourceType, Long sourceId);
}

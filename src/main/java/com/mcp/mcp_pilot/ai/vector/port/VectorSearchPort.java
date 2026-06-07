package com.mcp.mcp_pilot.ai.vector.port;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.vector.constant.SimilarityMetric;

import java.util.List;

public interface VectorSearchPort {

    List<Long> search(
            VectorTargetType targetType,
            String query,
            int topK,
            SimilarityMetric metric
    );
}

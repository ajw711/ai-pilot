package com.mcp.mcp_pilot.ai.vector.strategy;

import com.mcp.mcp_pilot.ai.vector.constant.SimilarityMetric;

public interface SimilarityCalculator {

    double calculate(float[] v1, float[] v2);

    SimilarityMetric getMetric();
}

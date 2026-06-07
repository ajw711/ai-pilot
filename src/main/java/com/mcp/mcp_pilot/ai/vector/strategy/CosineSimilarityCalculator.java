package com.mcp.mcp_pilot.ai.vector.strategy;

import com.mcp.mcp_pilot.ai.vector.constant.SimilarityMetric;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CosineSimilarityCalculator implements SimilarityCalculator {

    @Override
    public double calculate(float[] v1, float[] v2) {
        // 나중에 이 부분을 Java 25 Vector API (SIMD)로 교체
        double dotProduct = 0.0, n1 = 0.0, n2 = 0.0;
        for (int i=0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            n1 += v1[i] * v1[i];
            n2 += v2[i] * v2[i];
        }
        return dotProduct / (Math.sqrt(n1) * Math.sqrt(n2));
    }

    @Override
    public SimilarityMetric getMetric() {
        return SimilarityMetric.COSINE;
    }
}

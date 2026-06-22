package com.mcp.mcp_pilot.ai.vector.factory;

import com.mcp.mcp_pilot.ai.vector.constant.SimilarityMetric;
import com.mcp.mcp_pilot.ai.vector.strategy.SimilarityCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SimilarityCalculatorFactory {

    private final List<SimilarityCalculator> calculators;

    public SimilarityCalculator get(
            SimilarityMetric metric
    ) {

        return calculators.stream()
                .filter(c ->
                        c.getMetric() == metric
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "지원하지 않는 Metric : " + metric
                        )
                );
    }
}

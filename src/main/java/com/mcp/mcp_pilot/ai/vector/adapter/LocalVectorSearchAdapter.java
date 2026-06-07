package com.mcp.mcp_pilot.ai.vector.adapter;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.vector.constant.SimilarityMetric;
import com.mcp.mcp_pilot.ai.vector.dto.SearchResult;
import com.mcp.mcp_pilot.ai.vector.factory.SimilarityCalculatorFactory;
import com.mcp.mcp_pilot.ai.vector.repository.VectorStoreRepository;
import com.mcp.mcp_pilot.ai.vector.port.VectorSearchPort;
import com.mcp.mcp_pilot.ai.vector.strategy.SimilarityCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalVectorSearchAdapter implements VectorSearchPort {

    private final EmbeddingModel embeddingModel;
    private final VectorStoreRepository vectorStoreRepository;
    private final SimilarityCalculatorFactory calculatorFactory;

    private static final double SCORE_THRESHOLD = 0.7;

    @Override
    public List<Long> search(VectorTargetType targetType, String query, int topK,  SimilarityMetric metric) {
        // 검색어 임베딩 생성
        float[] queryVector =
                embeddingModel.embed(query);

        // Vector similarity search algorithms (Cosine Similarity, Euclidean Distance (L2), Dot Product (Inner Product))
        // 현재 설정된 Metric에 맞는 알고리즘 선택
        SimilarityCalculator calculator =
                calculatorFactory.get(
                        metric
                );

        return vectorStoreRepository
                .findByTargetType(targetType)
                .stream()
                .map(entity -> {

                    double score =
                            calculator.calculate(
                                    queryVector,
                                    entity.getEmbeddingVector()
                            );

                    return new SearchResult(
                            entity.getTargetId(),
                            score
                    );
                })
                .filter(result -> result.score() >= SCORE_THRESHOLD)
                .sorted((a, b) ->
                        Double.compare(
                                b.score(),
                                a.score()
                        )
                )
                .limit(topK)
                .map(SearchResult::id)
                .toList();
    }
}

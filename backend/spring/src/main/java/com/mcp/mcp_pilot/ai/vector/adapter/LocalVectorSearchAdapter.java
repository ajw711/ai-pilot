package com.mcp.mcp_pilot.ai.vector.adapter;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.vector.constant.SimilarityMetric;
import com.mcp.mcp_pilot.ai.vector.dto.VectorSearchResult;
import com.mcp.mcp_pilot.ai.vector.dto.VectorSearchTarget;
import com.mcp.mcp_pilot.ai.vector.factory.SimilarityCalculatorFactory;
import com.mcp.mcp_pilot.ai.vector.port.VectorSearchPort;
import com.mcp.mcp_pilot.ai.vector.repository.VectorStoreRepository;
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

    private static final double SCORE_THRESHOLD = 0.55;

    @Override
    public List<VectorSearchResult> search( VectorTargetType sourceType,
                              String query,
                              int topK,
                              SimilarityMetric metric) {
        // 검색어 임베딩 생성
        float[] queryVector = embeddingModel.embed(query);

        // Vector similarity search algorithms (Cosine Similarity, Euclidean Distance (L2), Dot Product (Inner Product))
        // 현재 설정된 Metric에 맞는 알고리즘 선택
        SimilarityCalculator calculator =
                calculatorFactory.get(
                        metric
                );

        List<VectorSearchTarget> targets = vectorStoreRepository.findSearchTargets(sourceType);
        log.info("[VectorSearch] DB에서 {}건 로드됨 (targetType={})", targets.size(), sourceType);

        return targets.stream()
                .map(target -> {
                    double score = calculator.calculate(queryVector, target.embeddingVector());
                    return new VectorSearchResult(
                            target.sourceId(),
                            target.chunkId(),
                            target.content(),
                            target.metadata(),
                            score
                    );
                })
                .filter(result -> result.score() >= SCORE_THRESHOLD)
                .sorted((a, b) -> Double.compare(b.score(), a.score()))
                .limit(topK)
                .toList();
    }
}

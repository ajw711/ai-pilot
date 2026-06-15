package com.mcp.mcp_pilot.knowledge.application.service;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.factory.AIClientFactory;
import com.mcp.mcp_pilot.ai.vector.constant.SimilarityMetric;
import com.mcp.mcp_pilot.ai.vector.port.VectorSearchPort;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.port.in.SearchKnowledgeUseCase;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgePersistencePort;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgeSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeSearchService implements SearchKnowledgeUseCase {

    private final KnowledgePersistencePort knowledgePersistencePort;
    private final KnowledgeSearchPort  knowledgeSearchPort;
    private final VectorSearchPort vectorSearchPort;

    @Override
    public String searchWiki(String query) {
        log.info("[SearchService] 검색 요청: {}", query);
        // 키워드 기반 검색
        List<KnowledgeLog> keywords = knowledgeSearchPort.search(query);
        if (!keywords.isEmpty()) {
            formatListResponse("키워드 검색 결괴", keywords);
        }

        // 유사도 검색
        List<Long> similarIds = vectorSearchPort.search(VectorTargetType.KNOWLEDGE, query, 3, SimilarityMetric.COSINE);
        if (!similarIds.isEmpty()) {
            List<KnowledgeLog> vectorResults = similarIds.stream()
                    .map(knowledgePersistencePort::findById)
                    .flatMap(Optional::stream)
                    .toList();
            return formatListResponse("유자 지식 검색 결과", vectorResults);
        }

        return "관련된 위키 내용을 찾을 수 없습니다.";
    }

    private String formatListResponse(String type, List<KnowledgeLog> list) {
        String titles = list.stream().map(k -> "- " + k.getTitle()).collect(Collectors.joining("\n"));
        return String.format("[%s]\\n%s\\n\\n상세 내용이 궁금하시면 제목을 정확히 말씀해주세요.", type, titles);
    }

}

package com.mcp.mcp_pilot.knowledge.service;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.vector.constant.SimilarityMetric;
import com.mcp.mcp_pilot.ai.vector.port.VectorSearchPort;
import com.mcp.mcp_pilot.knowledge.entity.KnowledgeLogEntity;
import com.mcp.mcp_pilot.knowledge.port.KnowledgeSearchPort;
import com.mcp.mcp_pilot.knowledge.repository.Knowledge.KnowledgeLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeSearchService {

    private final KnowledgeSearchPort searchPort; // 검색 어댑터 (현재 LIKE 기반)
    private final VectorSearchPort vectorSearchPort;
    private final KnowledgeLogRepository logRepository; // 원문 조회를 위한 레포지토리

    /**
     *
     * 위키 검색 로직 (하이브리드)
     *
     */
    public String searchWiki(String query) {
        log.info("[Wiki-Search] 검색 요청: {}", query);

        // 키워드 기반 검색 (Port/Adapter 기반 LIKE 검색)
        List<KnowledgeLogEntity> keywordResults = searchPort.search(query);
        if (!keywordResults.isEmpty()) {
            return formatListResponse("키워드 검색 결과", keywordResults);
        }

        // 키워드 결과가 없을 시 유사도 검색 호출
        List<Long> similarIds =
                vectorSearchPort.search(
                        VectorTargetType.KNOWLEDGE,
                        query,
                        3,
                        SimilarityMetric.COSINE
                );

        if (!similarIds.isEmpty()) {
            List<KnowledgeLogEntity> vectorResults = logRepository.findAllById(similarIds);
            return formatListResponse("유사 지식 검색 결과", vectorResults);
        }

        return "관련된 위키 내용을 찾을 수 없습니다.";
    }

    private String formatListResponse(String type, List<KnowledgeLogEntity> list) {
        String titles = list.stream()
                .map( k -> "- " + k.getTitle())
                .collect(Collectors.joining("\n"));
        return String.format("[%s]\n%s\n\n상세 내용이 궁금하시면 제목을 정확히 말씀해주세요.", type, titles);
    }
}

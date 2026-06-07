package com.mcp.mcp_pilot.knowledge.adapter;

import com.mcp.mcp_pilot.knowledge.entity.KnowledgeLogEntity;
import com.mcp.mcp_pilot.knowledge.repository.Knowledge.KnowledgeLogRepository;
import com.mcp.mcp_pilot.knowledge.port.KnowledgeSearchPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MVP 단계 검색 구현체
 *
 * 현재:
 * - Title LIKE 검색
 *
 * 향후:
 * - OpenSearch
 * - Elasticsearch
 * - Azure AI Search
 *
 * 등으로 교체 가능
 */
@Service
@RequiredArgsConstructor
public class KnowledgeLikeSearchAdapter implements KnowledgeSearchPort {

    private final KnowledgeLogRepository repository;


    @Override
    public List<KnowledgeLogEntity> search(String query) {
        // 제목(Title)에 키워드가 포함된 경우를 검색 (Index-less but practical for MVP)
        return repository.findByTitleContaining(query);
    }
}

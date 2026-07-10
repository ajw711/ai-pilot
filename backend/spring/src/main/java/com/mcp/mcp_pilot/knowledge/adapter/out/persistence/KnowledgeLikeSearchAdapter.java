package com.mcp.mcp_pilot.knowledge.adapter.out.persistence;

import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.mapper.KnowledgePersistenceMapper;
import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.repository.KnowledgeLogRepository;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgeSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeLikeSearchAdapter implements KnowledgeSearchPort {

    private final KnowledgeLogRepository logRepository;

    @Override
    public List<KnowledgeLog> search(String query) {
        log.info("[SearchAdapter] LIKE 검색 수행: {}", query);
        return logRepository.findByTitleContaining(query).stream()
                .map(KnowledgePersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<KnowledgeLog> findAll() {
        log.info("[SearchAdapter] 전체 목록 조회");
        return KnowledgePersistenceMapper.toDomainList(logRepository.findAllByOrderByUpdateDateDesc());
    }

    @Override
    public Optional<KnowledgeLog> findSummaryById(Long knowledgeId) {
        return logRepository.findById(knowledgeId).map(KnowledgePersistenceMapper::toDomain);
    }

    @Override
    public Optional<KnowledgeLog> findByTitle(String title) {
        return logRepository.findByTitle(title).map(KnowledgePersistenceMapper::toDomain);
    }

    @Override
    public List<KnowledgeLog> findByTitleContaining(String keyword) {
        return KnowledgePersistenceMapper.toDomainList(logRepository.findByTitleContaining(keyword));
    }
}

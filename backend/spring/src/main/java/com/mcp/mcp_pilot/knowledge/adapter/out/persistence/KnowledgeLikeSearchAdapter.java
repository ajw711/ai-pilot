package com.mcp.mcp_pilot.knowledge.adapter.out.persistence;

import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.mapper.KnowledgePersistenceMapper;
import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.repository.KnowledgeLogRepository;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgeSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeLikeSearchAdapter implements KnowledgeSearchPort {

    private final KnowledgeLogRepository logRepository;

    @Override
    public List<KnowledgeLog> search(String query) {
        log.info("[Persistence-Adapter] LIKE 검색 수행: {}", query);
        return logRepository.findByTitleContaining(query).stream()
                .map(KnowledgePersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }
}

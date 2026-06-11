package com.mcp.mcp_pilot.knowledge.adapter.out.persistence;

import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.entity.KnowledgeLogJpaEntity;
import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.mapper.KnowledgePersistenceMapper;
import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.repository.KnowledgeLogRepository;
import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.repository.KnowledgeSourceRepository;
import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.repository.KnowledgeTagRepository;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeSource;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeTag;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgePersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KnowledgePersistenceAdapter implements KnowledgePersistencePort {

    private final KnowledgeLogRepository logRepository;
    private final KnowledgeSourceRepository sourceRepository;
    private final KnowledgeTagRepository tagRepository;

    @Override
    public KnowledgeLog save(KnowledgeLog knowledgeLog) {
        KnowledgeLogJpaEntity entity = KnowledgePersistenceMapper.toEntity(knowledgeLog);
        KnowledgeLogJpaEntity savedEntity = logRepository.save(entity);
        return KnowledgePersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public void saveSources(List<KnowledgeSource> sources) {
        sourceRepository.saveAll(sources.stream()
                .map(KnowledgePersistenceMapper::toEntity)
                .collect(Collectors.toList()));
    }

    @Override
    public void saveTags(List<KnowledgeTag> tags) {
        tagRepository.saveAll(tags.stream()
                .map(KnowledgePersistenceMapper::toEntity)
                .collect(Collectors.toList()));
    }

    @Override
    public Optional<KnowledgeLog> findById(Long id) {
        return logRepository.findById(id).map(KnowledgePersistenceMapper::toDomain);
    }

    @Override
    public Optional<KnowledgeLog> findByTitle(String title) {
        return logRepository.findByTitle(title).map(KnowledgePersistenceMapper::toDomain);
    }

    @Override
    public List<KnowledgeLog> findByTitleContaining(String keyword) {
        return logRepository.findByTitleContaining(keyword).stream()
                .map(KnowledgePersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }
}

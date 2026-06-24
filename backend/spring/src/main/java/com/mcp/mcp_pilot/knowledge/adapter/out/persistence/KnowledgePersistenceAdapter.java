package com.mcp.mcp_pilot.knowledge.adapter.out.persistence;

import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.entity.KnowledgeLogJpaEntity;
import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.mapper.KnowledgePersistenceMapper;
import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.repository.KnowledgeLogRepository;
import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.repository.KnowledgeSourceRepository;
import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.repository.KnowledgeTagRepository;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeSource;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeTag;
import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;
import com.mcp.mcp_pilot.knowledge.domain.vo.VerificationReport;
import com.mcp.mcp_pilot.knowledge.exception.KnowledgeNotFoundException;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgePersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KnowledgePersistenceAdapter implements KnowledgePersistencePort {

    private final KnowledgeLogRepository logRepository;
    private final KnowledgeSourceRepository sourceRepository;
    private final KnowledgeTagRepository tagRepository;
    private final JsonMapper jsonMapper;

    @Override
    public KnowledgeLog save(KnowledgeLog knowledgeLog) {
        KnowledgeLogJpaEntity entity = KnowledgePersistenceMapper.toEntity(knowledgeLog);
        KnowledgeLogJpaEntity savedEntity = logRepository.save(entity);
        return KnowledgePersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<KnowledgeLog> findById(Long id) {
        return logRepository.findById(id).map(KnowledgePersistenceMapper::toDomain);
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
    public void updateStatus(Long knowledgeId, KnowledgeStatus status) {
        logRepository.updateStatus(knowledgeId, status);
    }

    @Override
    public void updateVerificationAndSummary(
            Long knowledgeId,
            String summary,
            Integer verificationScore,
            VerificationReport verificationReport,
            KnowledgeStatus status) {
        String reportJson = null;
        if (verificationReport != null) {
           try {
               reportJson = jsonMapper.writeValueAsString(verificationReport);
           } catch (JacksonException e) {
               throw new RuntimeException("검수 리포트 데이터 DB 전송 직렬화 실패", e);
           }
        }
        logRepository.updateVerificationAndSummary(knowledgeId, summary, verificationScore, reportJson, status);
    }

    @Override
    public void updatePublicationResult(Long knowledgeId, String notionPageId, String notionPageUrl) {
        KnowledgeLogJpaEntity entity = logRepository.findById(knowledgeId)
                .orElseThrow(() -> new KnowledgeNotFoundException(knowledgeId));
        entity.updatePublicationResult(notionPageId, notionPageUrl);
    }

    @Override
    public boolean isPublished(Long knowledgeId) {
        return logRepository.findById(knowledgeId)
                .map(entity -> entity.getNotionPageId() != null)
                .orElse(false);
    }

}

package com.mcp.mcp_pilot.knowledge.adapter.out.persistence.mapper;

import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.entity.KnowledgeLogJpaEntity;
import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.entity.KnowledgeSourceJpaEntity;
import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.entity.KnowledgeTagJpaEntity;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeSource;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeTag;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Persistence Mapper: Domain Entity <-> JPA Entity
 */
public class KnowledgePersistenceMapper {

    public static KnowledgeLog toDomain(KnowledgeLogJpaEntity entity) {
        return new KnowledgeLog(
                entity.getId(),
                entity.getTitle(),
                entity.getRawContent(),
                entity.getSummarizedContent(),
                entity.getNotionPageId(),
                entity.getCreateDate(),
                entity.getUpdateDate()
        );
    }

    public static KnowledgeLogJpaEntity toEntity(KnowledgeLog domain) {
        KnowledgeLogJpaEntity entity = KnowledgeLogJpaEntity.createLog(
                domain.getTitle(),
                domain.getRawContent(),
                domain.getSummarizedContent(),
                domain.getNotionPageId()
        );
        // ID가 있는 경우 (업데이트 시) 세팅
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        return entity;
    }

    public static KnowledgeSource toDomain(KnowledgeSourceJpaEntity entity) {
        return new KnowledgeSource(
                entity.getId(),
                entity.getKnowledgeLogId(),
                entity.getSourceUrl()
        );
    }

    public static KnowledgeSourceJpaEntity toEntity(KnowledgeSource domain) {
        KnowledgeSourceJpaEntity entity = KnowledgeSourceJpaEntity.createSource(
                domain.getKnowledgeLogId(),
                domain.getSourceUrl()
        );
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        return entity;
    }

    public static KnowledgeTag toDomain(KnowledgeTagJpaEntity entity) {
        return new KnowledgeTag(
                entity.getId(),
                entity.getKnowledgeLogId(),
                entity.getTagName()
        );
    }

    public static KnowledgeTagJpaEntity toEntity(KnowledgeTag domain) {
        KnowledgeTagJpaEntity entity = KnowledgeTagJpaEntity.createTag(
                domain.getKnowledgeLogId(),
                domain.getTagName()
        );
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        return entity;
    }
}

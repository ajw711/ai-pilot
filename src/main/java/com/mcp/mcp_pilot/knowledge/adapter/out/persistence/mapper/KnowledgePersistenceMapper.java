package com.mcp.mcp_pilot.knowledge.adapter.out.persistence.mapper;

import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.entity.KnowledgeLogJpaEntity;
import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.entity.KnowledgeSourceJpaEntity;
import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.entity.KnowledgeTagJpaEntity;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeSource;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeTag;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Persistence Mapper: Domain Entity <-> JPA Entity
 * 헥사고날 아키텍처 원칙에 따라 Adapter 레이어에서 경계 간 변환을 담당함.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgePersistenceMapper {

    // --- KnowledgeLog ---

    public static KnowledgeLog toDomain(KnowledgeLogJpaEntity entity) {
        if (entity == null) return null;
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
        if (domain == null) return null;
        KnowledgeLogJpaEntity entity = KnowledgeLogJpaEntity.create(
                domain.getTitle(),
                domain.getRawContent(),
                domain.getSummarizedContent(),
                domain.getNotionPageId()
        );
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        return entity;
    }

    public static List<KnowledgeLog> toDomainList(List<KnowledgeLogJpaEntity> entities) {
        return entities.stream()
                .map(KnowledgePersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    // --- KnowledgeSource ---

    public static KnowledgeSource toDomain(KnowledgeSourceJpaEntity entity) {
        if (entity == null) return null;
        return new KnowledgeSource(
                entity.getId(),
                entity.getKnowledgeLogId(),
                entity.getSourceUrl()
        );
    }

    public static KnowledgeSourceJpaEntity toEntity(KnowledgeSource domain) {
        if (domain == null) return null;
        KnowledgeSourceJpaEntity entity = KnowledgeSourceJpaEntity.create(
                domain.getKnowledgeLogId(),
                domain.getSourceUrl()
        );
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        return entity;
    }

    public static List<KnowledgeSource> toSourceDomainList(List<KnowledgeSourceJpaEntity> entities) {
        return entities.stream()
                .map(KnowledgePersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    // --- KnowledgeTag ---

    public static KnowledgeTag toDomain(KnowledgeTagJpaEntity entity) {
        if (entity == null) return null;
        return new KnowledgeTag(
                entity.getId(),
                entity.getKnowledgeLogId(),
                entity.getTagName()
        );
    }

    public static KnowledgeTagJpaEntity toEntity(KnowledgeTag domain) {
        if (domain == null) return null;
        KnowledgeTagJpaEntity entity = KnowledgeTagJpaEntity.create(
                domain.getKnowledgeLogId(),
                domain.getTagName()
        );
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        return entity;
    }

    public static List<KnowledgeTag> toTagDomainList(List<KnowledgeTagJpaEntity> entities) {
        return entities.stream()
                .map(KnowledgePersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }
}

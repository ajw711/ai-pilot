package com.mcp.mcp_pilot.document.adapter.out.persistence.mapper;

import com.mcp.mcp_pilot.document.adapter.out.persistence.entity.DocumentFileJpaEntity;
import com.mcp.mcp_pilot.document.domain.DocumentFile;
import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.entity.KnowledgeLogJpaEntity;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Persistence Mapper: Domain Entity <-> JPA Entity
 * Adapter 레이어에서 경계 간 변환을 담당함.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DocumentFilePersistenceMapper {

    public static DocumentFileJpaEntity toEntity(DocumentFile domain) {
        if (domain == null) return null;

        DocumentFileJpaEntity entity = DocumentFileJpaEntity.create(
                domain.getFileName(),
                domain.getContentType(),
                domain.getFileSize(),
                domain.getR2Key(),
                domain.getDocumentStatus(),
                domain.getUploadedBy(),
                domain.getDeleteAt()
        );

        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        return entity;
    }

    public static KnowledgeLog toDomain(KnowledgeLogJpaEntity entity) {
        if (entity == null) return null;
        return new KnowledgeLog(
                entity.getId(),
                entity.getTitle(),
                entity.getRawContent(),
                entity.getFormattedContent(),
                entity.getCreateDate(),
                entity.getUpdateDate(),
                entity.getVerificationScore(),
                entity.getVerificationReport(),
                entity.getStatus(),
                entity.getVerificationVersion(),
                entity.getDeletedAt()
        );
    }

    public static DocumentFile toDomain(DocumentFileJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new DocumentFile(
                entity.getId(),
                entity.getFileName(),
                entity.getContentType(),
                entity.getFileSize(),
                entity.getR2Key(),
                entity.getStatus(),
                entity.getCreateDate(),
                entity.getUpdateDate(),
                entity.getUploadedBy(),
                entity.getDeletedAt()
        );
    }
}

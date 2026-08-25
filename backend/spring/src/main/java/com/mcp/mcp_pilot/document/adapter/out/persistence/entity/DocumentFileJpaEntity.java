package com.mcp.mcp_pilot.document.adapter.out.persistence.entity;

import com.mcp.mcp_pilot.common.entitiy.BaseEntity;
import com.mcp.mcp_pilot.document.domain.vo.DocumentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@SQLRestriction("deleted_at IS NULL")
@Table(name = "document_file")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DocumentFileJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 500)
    private String r2Key;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DocumentStatus status;

    @Column(nullable = false)
    private Long uploadedBy;

    private LocalDateTime deletedAt;

    private DocumentFileJpaEntity(
            String fileName,
            String contentType,
            Long fileSize,
            String r2Key,
            DocumentStatus status,
            Long uploadedBy,
            LocalDateTime deletedAt
    ) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.r2Key = r2Key;
        this.status = status;
        this.uploadedBy = uploadedBy;
        this.deletedAt = deletedAt;
    }

    public static DocumentFileJpaEntity create(
            String fileName,
            String contentType,
            Long fileSize,
            String r2Key,
            DocumentStatus status,
            Long uploadedBy,
            LocalDateTime deletedAt
    ) {
        return new DocumentFileJpaEntity(
                fileName,
                contentType,
                fileSize,
                r2Key,
                status,
                uploadedBy,
                deletedAt
        );
    }
}

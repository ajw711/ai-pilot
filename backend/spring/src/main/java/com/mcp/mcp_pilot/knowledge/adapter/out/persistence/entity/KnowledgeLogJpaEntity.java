package com.mcp.mcp_pilot.knowledge.adapter.out.persistence.entity;

import com.mcp.mcp_pilot.common.entitiy.BaseEntity;
import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;


/**
 *  지식 원문 및 요약 (JPA Entity)
 */
@SQLRestriction("deleted_at IS NULL")
@Table(name = "knowledge_log")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeLogJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String rawContent; // 사용자가 입력한 날 것 데이터

    @Column(columnDefinition = "TEXT")
    private String formattedContent; // Gemini가 요약한 데이터

    private String notionPageId; // 노션에 성공적으로 적재 한 페이지 ID

    private String notionPageUrl; // 노션 페이지 URL

    private Integer verificationScore;

    @Column(columnDefinition = "TEXT")
    private String verificationReport;

    @Enumerated(EnumType.STRING)
    private KnowledgeStatus status;

    private Integer verificationVersion;

    private LocalDateTime deletedAt;

    private KnowledgeLogJpaEntity(String title,
                                  String rawContent,
                                  String formattedContent,
                                  String notionPageId,
                                  String notionPageUrl,
                                  Integer verificationScore,
                                  String verificationReport,
                                  KnowledgeStatus status,
                                  Integer verificationVersion,
                                  LocalDateTime deletedAt) {
        this.title = title;
        this.rawContent = rawContent;
        this.formattedContent = formattedContent;
        this.notionPageId = notionPageId;
        this.notionPageUrl = notionPageUrl;
        this.verificationScore = verificationScore;
        this.verificationReport = verificationReport;
        this.status = status;
        this.verificationVersion = verificationVersion;
        this.deletedAt = deletedAt;
    }

    public static KnowledgeLogJpaEntity create(String title,
                                               String rawContent,
                                               String formattedContent,
                                               String notionPageId,
                                               String notionPageUrl,
                                               Integer verificationScore,
                                               String verificationReport,
                                               KnowledgeStatus status,
                                               Integer verificationVersion,
                                               LocalDateTime deletedAt) {
        return new KnowledgeLogJpaEntity (title,
                rawContent,
                formattedContent,
                notionPageId,
                notionPageUrl,
                verificationScore,
                verificationReport,
                status,
                verificationVersion,
                deletedAt);
    }

    public void updatePublicationResult(String notionPageId, String notionPageUrl) {
        this.notionPageId = notionPageId;
        this.notionPageUrl = notionPageUrl;
    }
}

package com.mcp.mcp_pilot.knowledge.adapter.out.persistence.entity;

import com.mcp.mcp_pilot.common.entitiy.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 *  출처/근거 (JPA Entity)
 */
@Table(name = "knowledge_source")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeSourceJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "knowledge_log_id", nullable = false)
    private Long knowledgeLogId;

    @Column(nullable = false, length = 1000)
    private String sourceUrl;

    private KnowledgeSourceJpaEntity(Long knowledgeLogId, String sourceUrl) {
        this.knowledgeLogId = knowledgeLogId;
        this.sourceUrl = sourceUrl;
    }

    public static KnowledgeSourceJpaEntity createSource(Long knowledgeLogId, String sourceUrl) {
        return new KnowledgeSourceJpaEntity(knowledgeLogId, sourceUrl);
    }
}


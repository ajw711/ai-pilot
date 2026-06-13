package com.mcp.mcp_pilot.knowledge.adapter.out.persistence.entity;

import com.mcp.mcp_pilot.common.entitiy.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 *  기술 태그 (JPA Entity)
 */
@Table(name = "knowledge_tag")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeTagJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "knowledge_log_id", nullable = false)
    private Long knowledgeLogId;

    @Column(nullable = false, length = 100)
    private String tagName;

    private KnowledgeTagJpaEntity(Long knowledgeLogId, String tagName) {
        this.knowledgeLogId = knowledgeLogId;
        this.tagName = tagName.trim().toLowerCase();
    }

    public static KnowledgeTagJpaEntity create(Long knowledgeLogId, String tagName) {
        return new KnowledgeTagJpaEntity(knowledgeLogId, tagName);
    }
}


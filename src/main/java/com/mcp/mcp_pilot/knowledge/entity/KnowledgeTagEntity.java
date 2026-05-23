package com.mcp.mcp_pilot.knowledge.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *  기술 태그
 *  k8s, java, go 처럼 지식을 분류할 키워드
 *  1:N
 */
@Table(name = "knowledge_tag")
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeTagEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "knowledge_log_id", nullable = false)
    private Long knowledgeLogId;

    @Column(nullable = false, length = 100)
    private String tagName; // 태그 명칭 (예: "k8s", "java", "go")

    private KnowledgeTagEntity(Long knowledgeLogId, String tagName) {
        this.knowledgeLogId = knowledgeLogId;
        // 공백 제거 및 소문자로 정규화로 데이터 일관성을 유지
        this.tagName = tagName.trim().toLowerCase();
    }

    public static KnowledgeTagEntity createTag(Long knowledgeLogId, String tagName) {
        return new KnowledgeTagEntity(knowledgeLogId, tagName);
    }
}

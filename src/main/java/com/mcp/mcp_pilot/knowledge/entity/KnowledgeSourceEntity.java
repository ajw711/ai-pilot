package com.mcp.mcp_pilot.knowledge.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *  출처/근거
 *  지식 마스터 하나에 여러 개의 공식 문서 링크나 레퍼런스 URL이 붙을 수 있는 구조
 *  1:N
 */
@Table(name = "knowledge_source")
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeSourceEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @ManyToOne KnowledgeLog knowledgeLog를 거는 순간,
     * 나중에 대량 데이터를 적재하거나 배치 처리를 할 때 영속성 컨텍스트가 무거워지고
     * 불필요한 Select 쿼리(N+1 문제)가 유발 가능성이 있음
     */
    @Column(name = "knowledge_log_id", nullable = false)
    private Long knowledgeLogId;

    @Column(nullable = false, length = 1000)
    private String sourceUrl;

    private KnowledgeSourceEntity(Long knowledgeLogId, String sourceUrl) {
        this.knowledgeLogId = knowledgeLogId;
        this.sourceUrl = sourceUrl;
    }

    public static KnowledgeSourceEntity createSource(Long knowledgeLogId, String sourceUrl) {
        return new KnowledgeSourceEntity(knowledgeLogId, sourceUrl);
    }
}

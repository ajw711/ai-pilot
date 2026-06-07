package com.mcp.mcp_pilot.knowledge.entity;

import com.mcp.mcp_pilot.common.entitiy.BaseEntity;
import jakarta.persistence.*;
import lombok.*;


/**
 *  지식 원문 및 요약
 *  지식 마스터
 */
@Table(name = "knowledge_log")
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeLogEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String rawContent; // 사용자가 입력한 날 것 데이터

    @Column(columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String summarizedContent; // Gemini가 요약한 데이터

    private String notionPageId; // 노션에 성공적으로 적재 한 페이지 ID

    private KnowledgeLogEntity(String title, String rawContent, String summarizedContent, String notionPageId) {
        this.title = title;
        this.rawContent = rawContent;
        this.summarizedContent = summarizedContent;
        this.notionPageId = notionPageId;
    }

    public static KnowledgeLogEntity createLog(String title, String rawContent, String summarizedContent, String notionPageId) {
        return new KnowledgeLogEntity (title, rawContent, summarizedContent, notionPageId);
    }
}

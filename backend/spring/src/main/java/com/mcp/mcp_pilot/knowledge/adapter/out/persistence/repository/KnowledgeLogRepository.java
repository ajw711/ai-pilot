package com.mcp.mcp_pilot.knowledge.adapter.out.persistence.repository;

import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.entity.KnowledgeLogJpaEntity;
import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeLogRepository extends JpaRepository<KnowledgeLogJpaEntity, Long> {

    // 제목 정확도 매칭 (Exact Match)
    Optional<KnowledgeLogJpaEntity> findByTitle(String title);

    // 제목 키워드 검색 (LIKE %query%)
    List<KnowledgeLogJpaEntity> findByTitleContaining(String keyword);

    @Modifying
    @Query("""
        update KnowledgeLogJpaEntity k
            set k.formattedContent = :summary
        where k.id = :knowledgeId
    """)
    void updateSummary(
            @Param("knowledgeId") Long knowledgeId,
            @Param("summary") String summary
    );

    @Modifying
    @Query("""
        update KnowledgeLogJpaEntity k
            set k.status = :status
        where k.id = :knowledgeId
    """)
    void updateStatus(
            @Param("knowledgeId") Long knowledgeId,
            @Param("status") KnowledgeStatus status
    );

    @Modifying
    @Query("""
        update KnowledgeLogJpaEntity k
            set k.formattedContent = :summary,
                k.verificationScore = :verificationScore,
                k.verificationReport = :verificationReport,
                k.status = :status,
                k.verificationVersion = COALESCE(k.verificationVersion, 0) + 1
        where k.id = :knowledgeId
    """)
    void updateVerificationAndSummary(
            @Param("knowledgeId") Long knowledgeId,
            @Param("summary") String summary,
            @Param("verificationScore") Integer verificationScore,
            @Param("verificationReport") String reportJson,
            @Param("status") KnowledgeStatus status
    );

    List<KnowledgeLogJpaEntity> findAllByOrderByUpdateDateDesc();
}

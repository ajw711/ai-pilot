package com.mcp.mcp_pilot.knowledge.adapter.out.persistence.repository;

import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.entity.KnowledgeLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeLogRepository extends JpaRepository<KnowledgeLogJpaEntity, Long> {

    // 제목 정확도 매칭 (Exact Match)
    Optional<KnowledgeLogJpaEntity> findByTitle(String title);

    // 제목 키워드 검색 (LIKE %query%)
    List<KnowledgeLogJpaEntity> findByTitleContaining(String keyword);
}

package com.mcp.mcp_pilot.knowledge.repository.Knowledge;

import com.mcp.mcp_pilot.knowledge.entity.KnowledgeLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeLogRepository extends JpaRepository<KnowledgeLogEntity, Long> {

    // 제목 정확도 매칭 (Exact Match)
    Optional<KnowledgeLogEntity> findByTitle(String title);

    // 제목 키워드 검색 (LIKE %query%)
    List<KnowledgeLogEntity> findByTitleContaining(String keyword);
}

package com.mcp.mcp_pilot.knowledge.adapter.out.persistence.repository;

import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.entity.KnowledgeTagJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeTagRepository extends JpaRepository<KnowledgeTagJpaEntity, Long> {
    List<KnowledgeTagJpaEntity> findByKnowledgeLogId(Long knowledgeLogId);
}

package com.mcp.mcp_pilot.knowledge.adapter.out.persistence.repository;

import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.entity.KnowledgeSourceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeSourceRepository extends JpaRepository<KnowledgeSourceJpaEntity, Long> {
    List<KnowledgeSourceJpaEntity> findByKnowledgeLogId(Long knowledgeLogId);
}

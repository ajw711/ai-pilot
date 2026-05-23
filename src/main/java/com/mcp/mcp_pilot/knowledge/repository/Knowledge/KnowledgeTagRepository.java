package com.mcp.mcp_pilot.knowledge.repository.Knowledge;

import com.mcp.mcp_pilot.knowledge.entity.KnowledgeTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeTagRepository extends JpaRepository<KnowledgeTagEntity, Long> {
}

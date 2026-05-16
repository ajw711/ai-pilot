package com.mcp.mcp_pilot.repository;

import com.mcp.mcp_pilot.entity.KnowledgeLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeLogRepository extends JpaRepository<KnowledgeLogEntity, Long> {
}

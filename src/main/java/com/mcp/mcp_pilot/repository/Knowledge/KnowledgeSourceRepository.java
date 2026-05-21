package com.mcp.mcp_pilot.repository.Knowledge;

import com.mcp.mcp_pilot.entity.KnowledgeSourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeSourceRepository extends JpaRepository<KnowledgeSourceEntity, Long> {
}

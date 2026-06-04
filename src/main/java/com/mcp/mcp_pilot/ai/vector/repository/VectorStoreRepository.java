package com.mcp.mcp_pilot.ai.vector.repository;

import com.mcp.mcp_pilot.ai.vector.entity.VectorStoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VectorStoreRepository extends JpaRepository<VectorStoreEntity, Long> {
}

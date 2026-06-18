package com.mcp.mcp_pilot.ai.vector.repository;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.vector.entity.VectorStoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VectorStoreRepository extends JpaRepository<VectorStoreEntity, Long> {
    List<VectorStoreEntity> findByTargetType(VectorTargetType targetType);
    boolean existsByTargetTypeAndTargetId(VectorTargetType targetType, Long targetId);
}

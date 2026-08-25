package com.mcp.mcp_pilot.ai.vector.repository;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.vector.entity.VectorSourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VectorSourceRepository extends JpaRepository<VectorSourceEntity, Long> {
    Optional<VectorSourceEntity> findBySourceTypeAndSourceId(VectorTargetType sourceType, Long sourceId);
    void deleteBySourceTypeAndSourceId(VectorTargetType sourceType, Long sourceId);
}

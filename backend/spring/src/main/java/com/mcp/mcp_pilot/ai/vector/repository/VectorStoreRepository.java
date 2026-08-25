package com.mcp.mcp_pilot.ai.vector.repository;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.vector.dto.VectorSearchTarget;
import com.mcp.mcp_pilot.ai.vector.entity.VectorStoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VectorStoreRepository extends JpaRepository<VectorStoreEntity, Long> {
    void deleteByVectorSourceId(Long vectorSourceId);
    
    @Query("""
            SELECT s.sourceId AS sourceId,
                   vs.id AS chunkId,
                   vs.content AS content,
                   vs.metadata AS metadata,
                   vs.embeddingVector AS embeddingVector
            FROM VectorStoreEntity vs
            JOIN VectorSourceEntity s ON vs.vectorSourceId = s.id
            WHERE s.sourceType = :sourceType
        """)
    List<VectorSearchTarget> findSearchTargets(@Param("sourceType") VectorTargetType sourceType);
}

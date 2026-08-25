package com.mcp.mcp_pilot.ai.vector.entity;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.common.entitiy.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

/**
 *  텍스트를 의미 벡터(embedding vector)로 변환
 *
 */
@Table(name = "vector_store")
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VectorStoreEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vector_source_id", nullable = false)
    private Long vectorSourceId;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(
            name = "embedding_vector",
            columnDefinition = "bytea",
            nullable = false
    )
    @JdbcTypeCode(SqlTypes.VARBINARY)
    private float[] embeddingVector;

    private VectorStoreEntity(
            Long vectorSourceId,
            Integer chunkIndex,
            String content,
            Map<String, Object> metadata,
            float[] embeddingVector
    ) {
        this.vectorSourceId = vectorSourceId;
        this.chunkIndex = chunkIndex;
        this.content = content;
        this.metadata = metadata;
        this.embeddingVector = embeddingVector;
    }

    public static VectorStoreEntity createChunk(
            Long vectorSourceId,
            int chunkIndex,
            String content,
            Map<String, Object> metadata,
            float[] embeddingVector
    ) {
        return new VectorStoreEntity(
                vectorSourceId,
                chunkIndex,
                content,
                metadata,
                embeddingVector
        );
    }
}

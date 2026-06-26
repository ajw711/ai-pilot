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

    @Enumerated(EnumType.STRING)
    private VectorTargetType targetType; // 예: "KNOWLEDGE", "K8S_LOG"

    @Column(nullable = false)
    private Long targetId; // 도메인 pk

    @Column(name = "embedding_vector", columnDefinition = "bytea", nullable = false)
    @JdbcTypeCode(SqlTypes.VARBINARY)
    private float[] embeddingVector; // 768차원 벡터 좌표 배열

    private VectorStoreEntity(VectorTargetType targetType, Long targetId, float[] embeddingVector) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.embeddingVector = embeddingVector;
    }

    public static VectorStoreEntity createVectorStore(VectorTargetType targetType, Long targetId, float[] embeddingVector) {
        return new VectorStoreEntity(targetType, targetId, embeddingVector);
    }

}

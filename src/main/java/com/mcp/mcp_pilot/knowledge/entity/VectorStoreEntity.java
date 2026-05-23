package com.mcp.mcp_pilot.knowledge.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(name = "knowledge_log_id", nullable = false, unique = true)
    private Long knowledgeLogId;

    @Lob
    @Column(name = "embedding_vector", columnDefinition = "varbinary(max)", nullable = false)
    private float[] embeddingVector; // 768차원 벡터 좌표 배열

    private VectorStoreEntity(Long knowledgeLogId, float[] embeddingVector) {
        this.knowledgeLogId = knowledgeLogId;
        this.embeddingVector = embeddingVector;
    }

    public static VectorStoreEntity createVectorStore(Long knowledgeLogId, float[] embeddingVector) {
        return new VectorStoreEntity(knowledgeLogId, embeddingVector);
    }

}

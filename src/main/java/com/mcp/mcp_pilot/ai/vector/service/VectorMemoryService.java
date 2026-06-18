package com.mcp.mcp_pilot.ai.vector.service;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.exception.AiException;
import com.mcp.mcp_pilot.ai.vector.entity.VectorStoreEntity;
import com.mcp.mcp_pilot.ai.vector.repository.VectorStoreRepository;
import com.mcp.mcp_pilot.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;


@Slf4j
@Service
@RequiredArgsConstructor
public class VectorMemoryService {

    private final EmbeddingModel embeddingModel;
    private final TransactionTemplate transactionTemplate;
    private final VectorStoreRepository vectorStoreRepository;

    /**
     * 임베딩 생성 후 저장
     * 외부 API 호출(Network I/O)과 DB 저장(DB I/O)을 분리하여
     *  트랜잭션 점유 시간을 최소화
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveEmbedding(VectorTargetType targetType, Long targetId, String content) {
        // 외부 API 호출 (Network I/O)는 트랜잭션 외부에서 진행하여 DB 커넥션 점유 시간을 최소화
        float[] vector = generateVector(targetType, targetId, content);
        if (!exists(targetType, targetId)) {
            saveVectorToDatabase(targetType, targetId, vector);
        }
    }

    public boolean exists(VectorTargetType targetType, Long targetId) {
        return vectorStoreRepository.existsByTargetTypeAndTargetId(targetType, targetId);
    }

    /**
     * Embedding API 호출
     */
    private float[] generateVector(VectorTargetType targetType, Long targetId, String content) {
        try {
            log.info("[Vector] 임베딩 생성 시작 - Type: {}, ID: {}", targetType, targetId);
            return embeddingModel.embed(content);
        } catch (Exception e) {
            log.error("[Vector] 외부 API 통신 실패: {}", e.getMessage());
            throw new AiException(ErrorCode.AI_EMBEDDING_FAILURE, e);
        }
    }

    /**
     * Vector 저장
     */
    private void saveVectorToDatabase(VectorTargetType targetType, Long targetId, float[] vector) {
        try {
            VectorStoreEntity entity =
                    VectorStoreEntity.createVectorStore(
                            targetType,
                            targetId,
                            vector
                    );
            vectorStoreRepository.save(entity);

            log.info("after flush");
        } catch (Exception e) {
            log.error("[Vector] 벡터 저장 실패: {}", e.getMessage());
            throw new AiException(ErrorCode.AI_VECTOR_STORAGE_FAILURE, e);
        }
    }

}

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
    public void saveEmbedding(VectorTargetType targetType, Long targetId, String content) {
        // 외부 API 호출 (Network I/O)는 트랜잭션 외부에서 진행하여 DB 커넥션 점유 시간을 최소화
        float[] vector = generateVector(targetType, targetId, content);

        // TransactionTemplate 사용 이유:
        // 1. 서비스 내부 호출 시 @Transactional이 작동하지 않는 AOP 프록시 한계
        // 2. 서비스 레이어 분리 없이 명확한 트랜잭션 경계 설정
        transactionTemplate.executeWithoutResult(status -> {
            saveVectorToDatabase(targetType, targetId, vector);
        });
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
            VectorStoreEntity entity = VectorStoreEntity.createVectorStore(targetType, targetId, vector);
            vectorStoreRepository.save(entity);
            log.info("[Vector] 임베딩 저장 완료");
        } catch (Exception e) {
            log.error("[Vector] 벡터 저장 실패: {}", e.getMessage());
            throw new AiException(ErrorCode.AI_VECTOR_STORAGE_FAILURE, e);
        }
    }

}

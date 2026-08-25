package com.mcp.mcp_pilot.ai.vector.service;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.exception.AiException;
import com.mcp.mcp_pilot.ai.vector.dto.EmbeddedChunk;
import com.mcp.mcp_pilot.ai.vector.dto.RawChunk;
import com.mcp.mcp_pilot.ai.vector.port.VectorIndexingUseCase;
import com.mcp.mcp_pilot.ai.vector.port.VectorStorePersistencePort;
import com.mcp.mcp_pilot.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorIndexingService implements VectorIndexingUseCase {

    private final EmbeddingModel embeddingModel;
    private final VectorStorePersistencePort vectorStorePersistencePort;

    /**
     * 임베딩 생성 후 저장
     * 외부 API 호출(Network I/O)과 DB 저장(DB I/O)을 분리하여
     *  트랜잭션 점유 시간을 최소화
     */
    @Override
    public void indexChunks(VectorTargetType sourceType, Long sourceId, List<RawChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            log.warn("[VectorIndexingService] 색인할 청크가 없습니다. sourceType={}, sourceId={}", sourceType, sourceId);
            return;
        }

        log.info("[VectorIndexingService] 임베딩 생성 시작. sourceType={}, sourceId={}, chunkCount={}",
                sourceType, sourceId, chunks.size());

        // Gemini 임베딩 생성 (DB 커넥션 점유 X)
        List<EmbeddedChunk> embeddedChunks = chunks.stream()
                .map(this::embed)
                .toList();

        // 별도 컴포넌트(어댑터) 호출하여 @Transactional 프록시 정상 동작
        vectorStorePersistencePort.saveChunks(sourceType, sourceId, embeddedChunks);

        log.info("[VectorIndexingService] 색인 완료. sourceType={}, sourceId={}, chunkCount={}",
                sourceType, sourceId, embeddedChunks.size());
    }

    @Override
    public void deleteIndex(VectorTargetType sourceType, Long sourceId) {
        vectorStorePersistencePort.deleteIndex(sourceType, sourceId);
    }

    private EmbeddedChunk embed(RawChunk chunk) {
        try {
            float[] vector = embeddingModel.embed(chunk.content());
            return new EmbeddedChunk(
                    chunk.chunkIndex(),
                    chunk.content(),
                    chunk.metadata(),
                    vector
            );
        } catch (Exception e) {
            log.error("[VectorIndexingService] 임베딩 생성 실패. chunkIndex={}", chunk.chunkIndex(), e);
            throw new AiException(ErrorCode.AI_EMBEDDING_FAILURE, e);
        }
    }
}

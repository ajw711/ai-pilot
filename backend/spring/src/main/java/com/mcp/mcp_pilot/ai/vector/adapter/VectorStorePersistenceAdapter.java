package com.mcp.mcp_pilot.ai.vector.adapter;
import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.vector.dto.EmbeddedChunk;
import com.mcp.mcp_pilot.ai.vector.entity.VectorSourceEntity;
import com.mcp.mcp_pilot.ai.vector.entity.VectorStoreEntity;
import com.mcp.mcp_pilot.ai.vector.port.VectorStorePersistencePort;
import com.mcp.mcp_pilot.ai.vector.repository.VectorSourceRepository;
import com.mcp.mcp_pilot.ai.vector.repository.VectorStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VectorStorePersistenceAdapter implements VectorStorePersistencePort{

    private final VectorSourceRepository vectorSourceRepository;
    private final VectorStoreRepository vectorStoreRepository;

    @Override
    @Transactional
    public void saveChunks(VectorTargetType sourceType, Long sourceId, List<EmbeddedChunk> chunks) {
        VectorSourceEntity source = vectorSourceRepository
                .findBySourceTypeAndSourceId(sourceType, sourceId)
                .orElseGet(() -> vectorSourceRepository.save(VectorSourceEntity.of(sourceType, sourceId)));

        Long vectorSourceId = source.getId();
        vectorStoreRepository.deleteByVectorSourceId(vectorSourceId);

        List<VectorStoreEntity> entities = chunks.stream()
                .map(chunk -> VectorStoreEntity.createChunk(
                        vectorSourceId,
                        chunk.chunkIndex(),
                        chunk.content(),
                        chunk.metadata(),
                        chunk.vector()
                )).toList();
        vectorStoreRepository.saveAll(entities);
        log.info("[VectorStorePersistenceAdapter] DB 일괄 저장 완료. vectorSourceId={}, chunkCount={}", vectorSourceId, entities.size());
    }

    @Override
    @Transactional
    public void deleteIndex(VectorTargetType sourceType, Long sourceId) {
        vectorSourceRepository.deleteBySourceTypeAndSourceId(sourceType, sourceId);
        log.info("[VectorStorePersistenceAdapter] 벡터 색인 삭제 완료. sourceType={}, sourceId={}", sourceType, sourceId);
    }
}

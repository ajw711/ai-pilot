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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorIndexingService implements VectorIndexingUseCase {

    private final EmbeddingModel embeddingModel;
    private final VectorStorePersistencePort vectorStorePersistencePort;
    // 1회 API 호출당 묶어 보낼 청크 수 (Gemini 권장: 30 ~ 50)
    private static final int BATCH_SIZE = 40;
    // 배치 호출 간 딜레이(ms) - 분당 요청/토큰(RPM/TPM) 제한 방지
    private static final long BATCH_DELAY_MS = 300L;

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


        List<EmbeddedChunk> embeddedChunks = new ArrayList<>(chunks.size());

        // BATCH SIZE로 나누어 API 호출
        for (int i=0; i<chunks.size(); i+=BATCH_SIZE) {
            int toIndex = Math.min(i + BATCH_SIZE, chunks.size());
            List<RawChunk> batch = chunks.subList(i, toIndex);

            // batch에서 텍스트 추출 (chunks -> batch)
            List<String> contents = batch.stream()
                    .map(RawChunk::content)
                    .toList();

            try {
                // Spring AI 배치 임베딩 호출 (단 1번의 HTTP 요청으로 배치 처리)
                List<float[]> vectors = embeddingModel.embed(contents);

                if (vectors.size() !=batch.size()) {
                    throw new IllegalStateException(
                            String.format("임베딩 결과 개수(%d)가 요청 청크 개수(%d)와 일치하지 않습니다.",
                                    vectors.size(), batch.size())
                    );
                }

                // 임베딩 결과 매핑
                for (int j=0; j<batch.size(); j++) {
                    RawChunk chunk = batch.get(j);
                    embeddedChunks.add(new EmbeddedChunk(chunk.chunkIndex(), chunk.content(), chunk.metadata(), vectors.get(j)));
                }

                log.info("[VectorIndexingService] 배치 임베딩 진행 중... ({}/{})", toIndex, chunks.size());

                // Rate Limit 방지를 위한 배치 간 지연 (마지막 배치가 아닐 때만)
                applyDelay(toIndex, chunks.size());

            } catch (AiException aie) {
                throw aie;
            } catch (Exception e) {
                log.error("[VectorIndexingService] 배치 임베딩 실패. batchRange=[{}..{}]", i, toIndex - 1, e);
                throw new AiException(ErrorCode.AI_EMBEDDING_FAILURE, e);
            }
        }

        // 별도 컴포넌트(어댑터) 호출하여 @Transactional 프록시 정상 동작
        vectorStorePersistencePort.saveChunks(sourceType, sourceId, embeddedChunks);

        log.info("[VectorIndexingService] 색인 완료. sourceType={}, sourceId={}, chunkCount={}",
                sourceType, sourceId, embeddedChunks.size());
    }

    @Override
    public void deleteIndex(VectorTargetType sourceType, Long sourceId) {
        vectorStorePersistencePort.deleteIndex(sourceType, sourceId);
    }

    private void applyDelay(int currentIndex, int totalSize) {
        if (currentIndex < totalSize) {
            try {
                TimeUnit.MICROSECONDS.sleep(BATCH_DELAY_MS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.error("[VectorIndexingService] 임베딩 지연 대기 중 인터럽트 발생", ie);
                throw new AiException(ErrorCode.AI_EMBEDDING_FAILURE, ie);
            }
        }
    }
}

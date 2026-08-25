package com.mcp.mcp_pilot.ai.vector.migration;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.vector.dto.RawChunk;
import com.mcp.mcp_pilot.ai.vector.port.VectorIndexingUseCase;
import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.entity.KnowledgeLogJpaEntity;
import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.repository.KnowledgeLogRepository;
import com.mcp.mcp_pilot.knowledge.application.chunker.MarkdownChunker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VectorReindexRunner  implements ApplicationRunner {

    private final KnowledgeLogRepository knowledgeLogRepository;
    private final MarkdownChunker markdownChunker;
    private final VectorIndexingUseCase vectorIndexingUseCase;

    @Override
    public void run(ApplicationArguments args) {
        // --reindex 옵션이 없으면 실행하지 않고 바로 리턴
        if (!args.containsOption("reindex")) {
            log.info("[VectorReindexRunner] --reindex 옵션이 없으므로 재색인을 건너뜁니다.");
            return;
        }

        log.info("[VectorReindexRunner] 전체 Knowledge 재색인(Migration)을 시작합니다...");

        List<KnowledgeLogJpaEntity> allKnowledge = knowledgeLogRepository.findAll();
        log.info("[VectorReindexRunner] 대상 Knowledge 수: {}개", allKnowledge.size());

        int successCount = 0;
        int failCount = 0;

        for (KnowledgeLogJpaEntity knowledge : allKnowledge) {
            try {
                // formattedContent가 있으면 사용하고, 없으면 rawContent 사용
                String content = (knowledge.getFormattedContent() != null && !knowledge.getFormattedContent().
                        isBlank())
                        ? knowledge.getFormattedContent()
                        : knowledge.getRawContent();

                if (content == null || content.isBlank()) {
                    log.warn("[VectorReindexRunner] 내용이 비어있어 스킵 - ID: {}, Title: {}", knowledge.
                            getId(), knowledge.getTitle());
                    continue;
                }

                // MarkdownChunker를 통해 토큰/헤딩 기반 청킹
                List<RawChunk> chunks = markdownChunker.chunk(knowledge.getTitle(), content);
                if (chunks.isEmpty()) {
                    log.warn("[VectorReindexRunner] 청크 생성 결과 없음(0개) - ID: {}, Title: {}", knowledge.
                            getId(), knowledge.getTitle());
                    continue;
                }

                // VectorIndexingUseCase로 임베딩 생성 및 VectorSource/VectorStore 저장
                vectorIndexingUseCase.indexChunks(VectorTargetType.KNOWLEDGE, knowledge.getId(), chunks);
                successCount++;
                log.info("[VectorReindexRunner] 색인 완료 ({}/{}): ID={}, Title={}, Chunks={}",
                        successCount + failCount, allKnowledge.size(), knowledge.getId(), knowledge.getTitle(),
                        chunks.size());

            } catch (Exception e) {
                failCount++;
                log.error("[VectorReindexRunner] 색인 실패 - ID: {}, Title: {}, 원인: {}",
                        knowledge.getId(), knowledge.getTitle(), e.getMessage(), e);
            }
        }

        log.info("[VectorReindexRunner] 전체 Knowledge 재색인 완료! (성공: {}건, 실패: {}건)", successCount,
                failCount);
    }
}

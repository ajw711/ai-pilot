package com.mcp.mcp_pilot.knowledge.application.service;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.dto.AiRequest;
import com.mcp.mcp_pilot.ai.dto.ChatRequest;
import com.mcp.mcp_pilot.ai.dto.ChatResponse;
import com.mcp.mcp_pilot.ai.enums.AIModel;
import com.mcp.mcp_pilot.ai.factory.AIClientFactory;
import com.mcp.mcp_pilot.ai.strategy.AiClientStrategy;
import com.mcp.mcp_pilot.ai.vector.constant.SimilarityMetric;
import com.mcp.mcp_pilot.ai.vector.port.VectorSearchPort;
import com.mcp.mcp_pilot.common.enums.ToolType;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeSource;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeTag;
import com.mcp.mcp_pilot.knowledge.port.in.KnowledgeChatUseCase;
import com.mcp.mcp_pilot.knowledge.port.in.SaveKnowledgeUseCase;
import com.mcp.mcp_pilot.knowledge.port.in.SearchKnowledgeUseCase;
import com.mcp.mcp_pilot.knowledge.port.in.dto.SaveKnowledgeCommand;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgePersistencePort;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgeSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService implements SaveKnowledgeUseCase, SearchKnowledgeUseCase, KnowledgeChatUseCase {

    private final KnowledgePersistencePort persistencePort;
    private final KnowledgeSearchPort searchPort;
    private final VectorSearchPort vectorSearchPort;
    private final AIClientFactory aiClientFactory;

    @Override
    @Transactional
    public KnowledgeLog saveKnowledge(SaveKnowledgeCommand command) {
        log.info("지식 저장 프로세스 시작 (Application Service): {}", command.title());

        KnowledgeLog knowledgeLog = KnowledgeLog.create(
                command.title(),
                command.rawContent(),
                command.summarizedContent(),
                null
        );
        KnowledgeLog savedLog = persistencePort.save(knowledgeLog);
        Long knowledgeId = savedLog.getId();

        if (command.sourceUrls() != null && !command.sourceUrls().isEmpty()) {
            List<KnowledgeSource> sources = command.sourceUrls().stream()
                    .map(url -> KnowledgeSource.create(knowledgeId, url))
                    .collect(Collectors.toList());
            persistencePort.saveSources(sources);
        }

        if (command.tags() != null && !command.tags().isEmpty()) {
            List<KnowledgeTag> tags = command.tags().stream()
                    .map(tagName -> KnowledgeTag.create(knowledgeId, tagName))
                    .collect(Collectors.toList());
            persistencePort.saveTags(tags);
        }

        log.info("지식 저장 완료 ID: {}", knowledgeId);
        return savedLog;
    }

    @Override
    public String searchWiki(String query) {
        log.info("[Wiki-Search] 검색 요청 (Application Service): {}", query);

        // 1. 키워드 기반 검색 (Port 호출)
        List<KnowledgeLog> keywordResults = searchPort.search(query);
        if (!keywordResults.isEmpty()) {
            return formatListResponse("키워드 검색 결과", keywordResults);
        }

        // 2. 키워드 결과 없을 시 유사도 검색 호출
        List<Long> similarIds = vectorSearchPort.search(
                VectorTargetType.KNOWLEDGE,
                query,
                3,
                SimilarityMetric.COSINE
        );

        if (!similarIds.isEmpty()) {
            List<KnowledgeLog> vectorResults = similarIds.stream()
                    .map(persistencePort::findById)
                    .filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .collect(Collectors.toList());
            return formatListResponse("유사 지식 검색 결과", vectorResults);
        }

        return "관련된 위키 내용을 찾을 수 없습니다.";
    }

    @Override
    public ChatResponse chat(ChatRequest chatRequest) {
        log.info("Knowledge chat request (Application Service)");

        AiRequest aiRequest = AiRequest.of(
                chatRequest.message(),
                AIModel.GEMINI,
                List.of(
                        ToolType.STORE_KNOWLEDGE_DATA,
                        ToolType.SEARCH_KNOWLEDGE
                )
        );
        AiClientStrategy strategy = aiClientFactory.get(aiRequest.model());

        String answer = strategy.call(aiRequest);
        return ChatResponse.of(answer);
    }

    private String formatListResponse(String type, List<KnowledgeLog> list) {
        String titles = list.stream()
                .map(k -> "- " + k.getTitle())
                .collect(Collectors.joining("\n"));
        return String.format("[%s]\n%s\n\n상세 내용이 궁금하시면 제목을 정확히 말씀해주세요.", type, titles);
    }
}

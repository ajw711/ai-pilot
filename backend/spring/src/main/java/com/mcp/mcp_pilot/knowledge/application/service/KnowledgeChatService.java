package com.mcp.mcp_pilot.knowledge.application.service;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.vector.constant.SimilarityMetric;
import com.mcp.mcp_pilot.ai.vector.dto.VectorSearchResult;
import com.mcp.mcp_pilot.ai.vector.port.VectorSearchPort;
import com.mcp.mcp_pilot.knowledge.port.in.KnowledgeChatUseCase;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgeSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeChatService implements KnowledgeChatUseCase {

    private final ChatClient chatClient;
    private final VectorSearchPort vectorSearchPort;
    private final KnowledgeSearchPort knowledgeSearchPort;


    @Override
    public Flux<String> stream(String message, Long userId) {
        log.info("[KnowledgeChatService] RAG 스트리밍 요청 - userId: {}", userId);

        // 쿼리 임베딩 → 유사 지식 ID 검색
        List<VectorSearchResult> similarResults = vectorSearchPort.search(
                VectorTargetType.KNOWLEDGE, message, 3, SimilarityMetric.COSINE
        );
        log.info("[KnowledgeChatService] 유사 지식 {}건 검색됨: {}", similarResults.size(), similarResults);

        // 검색된 ID로 formattedContent 조회해서 Context 문자열 구성
        String context = similarResults.stream()
                .map(VectorSearchResult::sourceId)
                .distinct()
                .map(knowledgeSearchPort::findSummaryById)
                .flatMap(Optional::stream)
                .map(k -> "### " + k.getTitle() + "\n" + k.getFormattedContent())
                .collect(Collectors.joining("\n\n---\n\n"));

        // System Prompt에 Context 주입
        String systemPrompt = context.isBlank()
                ? "당신은 DevOps 지식 도우미입니다. 등록된 관련 지식이 없을 경우 일반 지식으로 답변하세요."
                : """
                  당신은 DevOps 지식 도우미입니다.
                  아래는 사용자의 개인 지식 저장소에서 검색된 관련 문서입니다.
                  이 내용을 최우선 참고하여 답변하세요. 출처가 있다면 언급하세요.
                  
                  [관련 지식]
                  """ + context;

        // Gemini 스트리밍 호출
        return chatClient.prompt()
                .system(systemPrompt)
                .user(message)
                .stream()
                .content();
    }
}

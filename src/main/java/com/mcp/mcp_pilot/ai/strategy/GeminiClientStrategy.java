package com.mcp.mcp_pilot.ai.strategy;

import com.mcp.mcp_pilot.ai.dto.AiRequest;
import com.mcp.mcp_pilot.knowledge.port.in.SaveKnowledgeUseCase;
import com.mcp.mcp_pilot.knowledge.port.in.dto.SaveKnowledgeCommand;
import com.mcp.mcp_pilot.knowledge.tool.KnowledgeTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Multi-step Strategy for Personal Wiki & Web Grounding (Optimized for Free Tier)
 */
@Slf4j
@Component("GEMINI")
@RequiredArgsConstructor
public class GeminiClientStrategy implements AiClientStrategy {

    private final ChatClient chatClient;
    private final KnowledgeTool knowledgeTool;
    private final SaveKnowledgeUseCase saveKnowledgeUseCase; // 직접 저장을 위해 유스케이스 주입


    @Override
    public String call(AiRequest request) {
        log.info("[Strategy] Gemini process start (Optimized): {}", request.message());

        // Step 1: Local Wiki Search (Only Function Calling)
        String localResult = chatClient.prompt()
                .system("너는 지식 관리 전문가야. 먼저 'searchKnowledge' 도구를 사용하여 내 개인 위키에서 정보를 찾아봐. " +
                        "만약 검색 결과가 없거나 정보가 부족하다면, 다른 부연 설명 없이 오직 '[NOT_FOUND]'라고만 답변해줘.")
                .user(request.message())
                .tools(knowledgeTool)
                .call()
                .content();

        log.info("[Strategy] Step 1 Result: {}", localResult);

        // [NOT_FOUND] 판정 시 2단계 진행
        if (localResult == null ||
                localResult.contains("[NOT_FOUND]") ||
                localResult.contains("결과가 없습니다") ||
                localResult.length() < 15) {

            log.info("[Strategy] Local Wiki Miss. Triggering Google Search Grounding...");

            // Step 2: Google Search Grounding + Summarization
            var groundingOptions = GoogleGenAiChatOptions.builder()
                    .googleSearchRetrieval(true)
                    .build();

            String webResult = chatClient.prompt()
                    .options(groundingOptions)
                    .system("너는 지식 큐레이터야. 구글 검색 결과를 바탕으로 사용자의 질문에 답해줘. " +
                            "답변은 반드시 '요약:' 섹션을 포함해야 하며, 위키에 저장하기 좋게 전문적인 톤으로 작성해줘.")
                    .user(request.message())
                    .call()
                    .content();

            log.info("[Strategy] Google Search result obtained. Saving to Wiki directly...");

            // Step 3 (Direct): DB에 직접 저장 (API 호출 없이 서버 로직으로 처리)
            try {
                saveKnowledgeUseCase.saveKnowledge(new SaveKnowledgeCommand(
                        request.message(), // 제목 (질문 키워드)
                        webResult,        // 원문
                        webResult,        // 요약 (일단 원문 사용, 향후 정교화 가능)
                        List.of("Auto-Saved", "Google-Grounding"),
                        List.of()
                ));
                log.info("[Strategy] Auto-Save success (No API Call used for saving)");
            } catch (Exception e) {
                log.error("[Strategy] Auto-Save failed: {}", e.getMessage());
            }

            return "[웹 검색 결과 - 위키 자동 저장됨]\n\n" + webResult;
        }

        log.info("[Strategy] Local Wiki Hit!");
        return localResult;
    }
}

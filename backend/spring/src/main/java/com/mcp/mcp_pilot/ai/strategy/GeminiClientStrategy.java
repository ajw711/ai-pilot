package com.mcp.mcp_pilot.ai.strategy;

import com.mcp.mcp_pilot.ai.dto.AiRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component("GEMINI")
@RequiredArgsConstructor
public class GeminiClientStrategy implements AiClientStrategy {

    private final ChatClient chatClient;

    @Override
    public String call(AiRequest request) {
        log.info("[Strategy] Gemini AI 호출 (순수 모드)");

        return chatClient.prompt()
                .system("너는 지식 관리 전문가야. 사용자의 질문에 전문적으로 답해줘.")
                .user(request.message())
                .call()
                .content();
    }
}

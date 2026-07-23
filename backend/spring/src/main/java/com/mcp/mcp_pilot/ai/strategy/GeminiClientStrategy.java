package com.mcp.mcp_pilot.ai.strategy;

import com.mcp.mcp_pilot.ai.dto.AiRequest;
import com.mcp.mcp_pilot.ai.registry.ToolRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component("GEMINI")
@RequiredArgsConstructor
public class GeminiClientStrategy implements AiClientStrategy {

    private final ChatClient chatClient;
    private final ToolRegistry toolRegistry;

    @Override
    public String call(AiRequest request) {
        log.info("[Strategy] Gemini AI 호출 (ToolRegistry 연동)");

        // 요청된 ToolType 리스트를 실제 스프링 툴 빈 배열로 동적 해석(Resolve)
        Object[] resolvedTools = toolRegistry.resolve(request.tools());

        return chatClient.prompt()
                .system("너는 Kubernetes 조작 및 개인 지식 관리를 돕는 지능형 플랫폼 비서(Pilot)야. " +
                        "사용자가 요청하는 바에 따라 제공되는 도구(Tool)들을 자유롭게 사용하여 일을 수행하고 답해줘.")
                .user(request.message())
                .tools(resolvedTools)
                .call()
                .content();
    }
}

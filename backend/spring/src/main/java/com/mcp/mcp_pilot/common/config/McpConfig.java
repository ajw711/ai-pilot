package com.mcp.mcp_pilot.common.config;

import com.mcp.mcp_pilot.knowledge.tool.KnowledgeTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

@Configuration
public class McpConfig {

    /**
     * ChatClient 빈 설정
     *
     * @param chatModel 사용할 AI 모델 (Gemini 등)
     * @param toolCallbackProviders 프로젝트 내에 존재하는 모든 도구 공급자 목록.
     *                              스프링 AI는 @Tool이 붙은 빈을 찾으면 자동으로 MethodToolCallbackProvider를 만들고,
     *                              @McpTool 관련 설정이 있으면 SyncMcpToolCallbackProvider를 만듭니다.
     *                              이들을 리스트로 모두 주입받아 통합 관리
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel, List<ToolCallbackProvider> toolCallbackProviders) {
        // 지역 변수 타입 추론.
        // 오른쪽의 ChatClient.builder()를 통해 타입을 명확히 알 수 있으므로 코드를 간결하게 작성합니다.
        var builder = ChatClient.builder(chatModel);


        // toolCallbackProviders.forEach(provider -> builder.defaultToolCallbacks(provider))와 동일한 의미
        // 이를 통해 로컬 @Tool 기반 도구와 MCP 기반 도구 모두를 ChatClient의 기본 도구로 등록
        toolCallbackProviders.forEach(builder::defaultToolCallbacks);
        System.out.println(toolCallbackProviders.size());
        return builder.build();
    }
}

package com.mcp.mcp_pilot.ai.strategy;

import com.mcp.mcp_pilot.ai.dto.AiRequest;
import com.mcp.mcp_pilot.ai.registry.ToolRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;

@Slf4j
@Component("GEMINI")
@RequiredArgsConstructor
public class GeminiClientStrategy implements AiClientStrategy {

    private final ChatClient chatClient;
    private final ToolRegistry toolRegistry;

    @Override
    public String call(AiRequest request) {
        log.info("[Strategy] call 호출 (ToolRegistry 연동)");

        // 요청된 ToolType 리스트를 실제 스프링 툴 빈 배열로 동적 해석(Resolve)
        Object[] resolvedTools = toolRegistry.resolve(request.tools());

        Map<String, Object> ctx = Map.of("userId", request.userId());
        log.info("[Strategy] toolContext 실제 값: {}", ctx);

        return chatClient.prompt()
                .system("너는 Kubernetes 조작 및 개인 지식 관리를 돕는 지능형 플랫폼 비서(Pilot)야. " +
                        "사용자가 요청하는 바에 따라 제공되는 도구(Tool)들을 자유롭게 사용하여 일을 수행하고 답해줘.")
                .user(request.message())
                .tools(resolvedTools)
                .toolContext(Map.of("userId", request.userId()))
                .call()
                .content();
    }

    @Override
    public Flux<String> streamCall(AiRequest request) {
        log.info("[Strategy] streamCall  호출");

        Object[] resolvedTools = toolRegistry.resolve(request.tools());

        Map<String, Object> ctx = Map.of("userId", request.userId());
        log.info("[Strategy] toolContext 실제 값: {}", ctx);

        return chatClient.prompt()
                .system("너는 Kubernetes 조작 및 개인 지식 관리를 돕는 지능형 플랫폼 비서(Pilot)야. " +
                        "사용자가 배포(Deploy) 요청을 하면, 되묻지 말고 주저 없이 제공된 DEPLOY_APP 도구를 즉시 실행해서 배포를 수행해줘.")
                .user(request.message())
                .tools(resolvedTools)
                .toolContext(Map.of("userId", request.userId()))
                .stream() // stream() 모드 전환
                .content(); // Flux<String> 리턴
    }
}

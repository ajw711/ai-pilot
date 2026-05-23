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
        log.info("Gemini client request: {}", request);

        return chatClient.prompt()
                .user(request.message())
                .tools(request.tools().toArray(new String[0]))
                .call()
                .content();
    }
}

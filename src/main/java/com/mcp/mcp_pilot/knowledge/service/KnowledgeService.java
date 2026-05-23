package com.mcp.mcp_pilot.knowledge.service;

import com.mcp.mcp_pilot.ai.dto.AiRequest;
import com.mcp.mcp_pilot.ai.dto.ChatRequest;
import com.mcp.mcp_pilot.ai.enums.AIModel;
import com.mcp.mcp_pilot.ai.factory.AIClientFactory;
import com.mcp.mcp_pilot.ai.strategy.AiClientStrategy;
import com.mcp.mcp_pilot.dto.chat.ChatResponse;
import com.mcp.mcp_pilot.knowledge.repository.Knowledge.KnowledgeLogRepository;
import com.mcp.mcp_pilot.knowledge.repository.Knowledge.KnowledgeSourceRepository;
import com.mcp.mcp_pilot.knowledge.repository.Knowledge.KnowledgeTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final AIClientFactory aiClientFactory;
    private final ChatClient chatClient;

    private final EmbeddingModel embeddingModel;

    public ChatResponse chat(ChatRequest chatRequest) {
        log.info("Knowledge chat request!!");
        AiRequest aiRequest = AiRequest.of(chatRequest.message(),)
        AiClientStrategy strategy = aiClientFactory.get(AIModel.GEMINI);

        String answer = strategy.call(AiRequest.of(
                chatRequest.message(),
                AIModel.GEMINI,
                List.of()
        ));
        return ChatResponse.of(answer);
    }
}

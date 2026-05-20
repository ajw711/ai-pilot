package com.mcp.mcp_pilot.service;

import com.mcp.mcp_pilot.dto.Knowledge.KnowledgeRequest;
import com.mcp.mcp_pilot.dto.chat.ChatRequest;
import com.mcp.mcp_pilot.dto.chat.ChatResponse;
import com.mcp.mcp_pilot.repository.KnowledgeLogRepository;
import com.mcp.mcp_pilot.repository.KnowledgeSourceRepository;
import com.mcp.mcp_pilot.repository.KnowledgeTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeChatService {

    private final KnowledgeLogRepository logRepository;
    private final KnowledgeSourceRepository sourceRepository;
    private final KnowledgeTagRepository tagRepository;
    private final ChatClient chatClient;

    private final EmbeddingModel embeddingModel;

    public ChatResponse chat(ChatRequest chatRequest) {
        log.info("Knowledge chat request!!");

        // LLM 호출
        // prompt()
        // user()
        // call()
        // 멀티스레드 문제에서 전부 요청마다 새 객체 체인 생성하는 방식이라 일반적으로 thread-safe하게 사용됨.
        String answer = chatClient.prompt()
                .user(chatRequest.message())
                .tools("storeKnowledgeData")
                .call()
                .content();

        return ChatResponse.of(answer);

    }
}

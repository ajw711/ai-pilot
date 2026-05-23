package com.mcp.mcp_pilot.knowledge.service;

import com.mcp.mcp_pilot.ai.dto.ChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;

    public String chat(ChatRequest chatRequest) {
        return chatClient.prompt()
                .
    }
}

package com.mcp.mcp_pilot.controller;

import com.mcp.mcp_pilot.dto.chat.ChatRequest;
import com.mcp.mcp_pilot.tool.KnowledgeTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v{version}/knowledge")
@RequiredArgsConstructor
public class McpChatController {

    private final ChatClient chatClient;
    private final KnowledgeTool knowledgeTool;

    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest chatRequest) {
        log.info(" chat request =  {} " , chatRequest.message());
        return null;
    }
}

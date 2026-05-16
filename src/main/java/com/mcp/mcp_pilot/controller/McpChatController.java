package com.mcp.mcp_pilot.controller;

import com.mcp.mcp_pilot.tool.KnowledgeTool;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v{version}/knowledge")
@RequiredArgsConstructor
public class McpChatController {

    private final ChatClient chatClient;
    private final KnowledgeTool knowledgeTool;

    @PostMapping("/chat")
    public String chat(@RequestParam("message") String message) {

    }
}

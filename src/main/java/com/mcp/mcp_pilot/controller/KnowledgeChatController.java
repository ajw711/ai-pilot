package com.mcp.mcp_pilot.controller;

import com.mcp.mcp_pilot.dto.chat.ChatRequest;
import com.mcp.mcp_pilot.dto.chat.ChatResponse;
import com.mcp.mcp_pilot.service.KnowledgeChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Controller
 *    ↓
 * ChatService
 *    ↓
 * Gemini
 *    ↓
 * @McpTool
 *    ↓
 * KnowledgeToolService
 *    ↓
 * Repository
 */
@Slf4j
@RestController
@RequestMapping("/api/v{version}/knowledge")
@RequiredArgsConstructor
public class KnowledgeChatController {

    private final KnowledgeChatService knowledgeChatService;

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest chatRequest) {
        log.info(" chat request !! ");
        return knowledgeChatService.chat(chatRequest);

    }
}

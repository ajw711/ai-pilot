package com.mcp.mcp_pilot.knowledge.controller;

import com.mcp.mcp_pilot.ai.dto.ChatRequest;
import com.mcp.mcp_pilot.ai.dto.ChatResponse;
import com.mcp.mcp_pilot.knowledge.service.KnowledgeService;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * --------
 * Frontend
 *    ↓
 * KnowledgeChatController
 *    ↓
 * KnowledgeService
 *    ↓
 * AIClientFactory
 *    ↓
 * GeminiClientStrategy
 *    ↓
 * Gemini API
 *    ↓
 * Tool Calling
 *    ↓
 * KnowledgeTool
 *    ↓
 * KnowledgeToolService
 *    ↓
 * Repository
 */
@Tag(name = "AI Chat", description = "에이전트 대화")
@Slf4j
@RestController
@RequestMapping("/api/{version}/knowledge")
@RequiredArgsConstructor
public class KnowledgeChatController {

    private final KnowledgeService knowledgeService;

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest chatRequest) {
        log.info(" chat request !!");
        return knowledgeService.chat(chatRequest);
    }
}

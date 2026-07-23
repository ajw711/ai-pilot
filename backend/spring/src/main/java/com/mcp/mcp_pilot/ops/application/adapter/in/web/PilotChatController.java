package com.mcp.mcp_pilot.ops.application.adapter.in.web;

import com.mcp.mcp_pilot.ai.dto.ChatRequest;
import com.mcp.mcp_pilot.ai.dto.ChatResponse;
import com.mcp.mcp_pilot.ops.port.in.PilotChatUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/{version}/pilot")
@RequiredArgsConstructor
public class PilotChatController {

    private final PilotChatUseCase pilotChatUseCase;
    
    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest chatRequest) {
        log.info("[PilotChatController] 운영 비서 챗 요청 수신");
        return pilotChatUseCase.chat(chatRequest);
    }
}

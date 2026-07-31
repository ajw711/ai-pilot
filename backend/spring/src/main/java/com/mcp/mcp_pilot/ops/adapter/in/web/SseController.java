package com.mcp.mcp_pilot.ops.adapter.in.web;

import com.mcp.mcp_pilot.common.dto.ApiResponse;
import com.mcp.mcp_pilot.common.security.CustomUserPrincipal;
import com.mcp.mcp_pilot.ops.adapter.in.web.dto.TicketResponse;
import com.mcp.mcp_pilot.ops.application.domain.entity.SseTicket;
import com.mcp.mcp_pilot.ops.port.in.IssueSseTicketUseCase;
import com.mcp.mcp_pilot.ops.port.in.dto.IssueSseTicketCommand;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/{version}/sse")
@RequiredArgsConstructor
public class SseController {

    private final IssueSseTicketUseCase issueSseTicketUseCase;

    @PostMapping("/ticket")
    public ApiResponse<TicketResponse> issueTicket(
            HttpServletRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        IssueSseTicketCommand command =
                new IssueSseTicketCommand(
                        principal.getId(),
                        request.getRemoteAddr(),
                        request.getHeader(HttpHeaders.USER_AGENT)
                );


        TicketResponse response = issueSseTicketUseCase.issue(command);
        return ApiResponse.success(response);
    }
}

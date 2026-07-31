package com.mcp.mcp_pilot.ops.application.service;

import com.mcp.mcp_pilot.common.exception.ErrorCode;
import com.mcp.mcp_pilot.ops.adapter.in.web.dto.TicketResponse;
import com.mcp.mcp_pilot.ops.application.domain.entity.SseTicket;
import com.mcp.mcp_pilot.ops.port.in.ConsumeSseTicketUseCase;
import com.mcp.mcp_pilot.ops.port.in.IssueSseTicketUseCase;
import com.mcp.mcp_pilot.ops.port.in.dto.IssueSseTicketCommand;
import com.mcp.mcp_pilot.ops.port.out.SseTicketGeneratorPort;
import com.mcp.mcp_pilot.ops.port.out.SseTicketRepositoryPort;
import com.mcp.mcp_pilot.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseTicketService implements IssueSseTicketUseCase, ConsumeSseTicketUseCase {

    private final SseTicketRepositoryPort sseTicketRepositoryPort;
    private final SseTicketGeneratorPort sseTicketGeneratorPort;

    @Override
    public TicketResponse issue(IssueSseTicketCommand command) {

        log.info(
                "[SseTicketService] SSE 티켓 발급. userId={}",
                command.userId()
        );

        String rawTicket = sseTicketGeneratorPort.generate();

        SseTicket ticket = SseTicket.builder()
                .ticket(rawTicket)
                .userId(command.userId())
                .clientIp(command.clientIp())
                .userAgent(command.userAgent())
                .expiresAt(Instant.now().plusSeconds(30))
                .build();

        sseTicketRepositoryPort.save(ticket);

        return new TicketResponse(rawTicket);
    }

    @Override
    public Long consume(String rawTicket) {

        log.info(
                "[SseTicketService] SSE 티켓 인증. rawTicket={}",
                rawTicket
        );

        SseTicket ticket = sseTicketRepositoryPort.findByTicket(rawTicket)
                .orElseThrow(() -> new UserException(ErrorCode.INVALID_TICKET));

        if (ticket.isExpired()) {
            sseTicketRepositoryPort.delete(rawTicket);
            throw new UserException(ErrorCode.EXPIRED_TICKET);
        }

        // 1회성 티켓 제거
        sseTicketRepositoryPort.delete(rawTicket);

        return ticket.getUserId();
    }
}
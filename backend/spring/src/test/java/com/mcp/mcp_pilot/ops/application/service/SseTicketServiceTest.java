package com.mcp.mcp_pilot.ops.application.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mcp.mcp_pilot.common.exception.ErrorCode;
import com.mcp.mcp_pilot.ops.adapter.in.web.dto.TicketResponse;
import com.mcp.mcp_pilot.ops.application.domain.entity.SseTicket;
import com.mcp.mcp_pilot.ops.port.in.dto.IssueSseTicketCommand;
import com.mcp.mcp_pilot.ops.port.out.SseTicketGeneratorPort;
import com.mcp.mcp_pilot.ops.port.out.SseTicketRepositoryPort;
import com.mcp.mcp_pilot.user.exception.UserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class SseTicketServiceTest {

    @Mock
    private SseTicketRepositoryPort sseTicketRepositoryPort;
    @Mock
    private SseTicketGeneratorPort sseTicketGeneratorPort;

    private SseTicketService sseTicketService;
    private final Long testUserId = 1L;
    private final String testIp = "127.0.0.1";
    private final String testUa = "Mozilla/5.0";

    @BeforeEach
    void setUp() {
        sseTicketService = new SseTicketService(sseTicketRepositoryPort, sseTicketGeneratorPort);
    }

    @Test
    @DisplayName("티켓 발급 요청 시 토큰 생성 및 영속 저장을 수행한다")
    void issueSuccess() {
        // Given
        IssueSseTicketCommand command = new IssueSseTicketCommand(testUserId, testIp, testUa);
        String mockRawToken = "MOCK-RANDOM-TOKEN-STRING-12345";
        when(sseTicketGeneratorPort.generate()).thenReturn(mockRawToken);

        // When
        TicketResponse response = sseTicketService.issue(command);

        // Then
        assertNotNull(response);
        assertEquals(mockRawToken, response.ticket());
        verify(sseTicketRepositoryPort, times(1)).save(any(SseTicket.class));
    }

    @Test
    @DisplayName("정상 티켓 소비 요청 시 사용자 ID를 반환하고 티켓을 데이터베이스에서 즉각 제거한다")
    void consumeSuccess() {
        // Given
        String mockRawToken = "MOCK-RANDOM-TOKEN-STRING-12345";
        SseTicket sseTicket = SseTicket.builder()
                .ticket(mockRawToken)
                .userId(testUserId)
                .clientIp(testIp)
                .userAgent(testUa)
                .expiresAt(Instant.now().plusSeconds(30))
                .build();
        when(sseTicketRepositoryPort.findByTicket(mockRawToken)).thenReturn(Optional.of(sseTicket));

        // When
        Long resultUserId = sseTicketService.consume(mockRawToken);

        // Then
        assertEquals(testUserId, resultUserId);
        verify(sseTicketRepositoryPort, times(1)).delete(mockRawToken);
    }

    @Test
    @DisplayName("존재하지 않는 티켓을 소비하려 하면 INVALID_TICKET 예외를 반환한다")
    void consumeInvalidTicketThrows() {
        // Given
        String invalidToken = "non-existent-token";
        when(sseTicketRepositoryPort.findByTicket(invalidToken)).thenReturn(Optional.empty());

        // When & Then
        UserException exception = assertThrows(UserException.class, () -> {
            sseTicketService.consume(invalidToken);
        });
        assertEquals(ErrorCode.INVALID_TICKET, exception.getErrorCode());
        verify(sseTicketRepositoryPort, never()).delete(anyString());
    }

    @Test
    @DisplayName("기한이 만료된 티켓을 소비하려 하면 EXPIRED_TICKET 예외를 반환하고 즉시 삭제한다")
    void consumeExpiredTicketThrows() {
        // Given
        String expiredToken = "expired-token";
        SseTicket sseTicket = SseTicket.builder()
                .ticket(expiredToken)
                .userId(testUserId)
                .clientIp(testIp)
                .userAgent(testUa)
                .expiresAt(Instant.now().minusSeconds(10)) // 이미 만료
                .build();
        when(sseTicketRepositoryPort.findByTicket(expiredToken)).thenReturn(Optional.of(sseTicket));

        // When & Then
        UserException exception = assertThrows(UserException.class, () -> {
            sseTicketService.consume(expiredToken);
        });
        assertEquals(ErrorCode.EXPIRED_TICKET, exception.getErrorCode());
        verify(sseTicketRepositoryPort, times(1)).delete(expiredToken);
    }
}
package com.mcp.mcp_pilot.user.application.service;

import com.mcp.mcp_pilot.user.port.in.dto.LogoutCommand;
import com.mcp.mcp_pilot.user.port.out.RefreshTokenPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock
    private RefreshTokenPort refreshTokenPort;

    @InjectMocks
    private LogoutService logoutService;

    @Test
    @DisplayName("유효한 리프레시 토큰이 입력되면 토큰 무효화(revoke) 포트가 정상 호출된다")
    void logout_Success() {
        // Given
        LogoutCommand command = new LogoutCommand("valid-refresh-token-hash");

        // When
        logoutService.logout(command);

        // Then
        verify(refreshTokenPort, times(1)).revoke("valid-refresh-token-hash");
    }

    @Test
    @DisplayName("리프레시 토큰이 null이거나 비어있으면 무효화 포트가 호출되지 않는다")
    void logout_Fail_EmptyToken() {
        // Given
        LogoutCommand commandNull = new LogoutCommand(null);
        LogoutCommand commandBlank = new LogoutCommand("");

        // When
        logoutService.logout(commandNull);
        logoutService.logout(commandBlank);

        // Then
        verifyNoInteractions(refreshTokenPort);
    }
}
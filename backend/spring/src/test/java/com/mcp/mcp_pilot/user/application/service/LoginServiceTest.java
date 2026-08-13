package com.mcp.mcp_pilot.user.application.service;

import com.mcp.mcp_pilot.common.exception.ErrorCode;
import com.mcp.mcp_pilot.user.domain.entitiy.User;
import com.mcp.mcp_pilot.user.domain.vo.Role;
import com.mcp.mcp_pilot.user.exception.UserException;
import com.mcp.mcp_pilot.user.port.in.dto.LoginCommand;
import com.mcp.mcp_pilot.user.port.out.RefreshTokenPort;
import com.mcp.mcp_pilot.user.port.out.TokenPort;
import com.mcp.mcp_pilot.user.port.out.UserPersistencePort;
import com.mcp.mcp_pilot.user.port.out.dto.TokenResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {
    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private RefreshTokenPort refreshTokenPort;

    @Mock
    private TokenPort tokenPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginService loginService;

    @Test
    @DisplayName("아이디와 비밀번호가 일치하면 로그인이 성공하고 토큰이 반환된다")
    void loginSuccess() {
        // Given
        LoginCommand command = new LoginCommand("test-user", "1234");
        User user = User.builder()
                .id(1L)
                .username("test-user")
                .password("encoded-1234")
                .role(Role.ADMIN)
                .build();
        TokenResult expectedToken = new TokenResult("access-token-xxx", "refresh-token-yyy");

        when(userPersistencePort.findByUsername("test-user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("1234", "encoded-1234")).thenReturn(true);
        when(tokenPort.generateTokens(user)).thenReturn(expectedToken);

        // When
        TokenResult result = loginService.login(command);

        // Then
        assertNotNull(result);
        assertEquals("access-token-xxx", result.accessToken());
        assertEquals("refresh-token-yyy", result.refreshToken());
        verify(userPersistencePort).findByUsername("test-user");
        verify(passwordEncoder).matches("1234", "encoded-1234");
        verify(tokenPort).generateTokens(user);
        verify(refreshTokenPort).save(eq(1L), eq("refresh-token-yyy"), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 아이디로 로그인 시도 시 UNAUTHORIZED_USER 예외가 발생한다")
    void login_Fail_UserNotFound() {
        // Given
        LoginCommand command = new LoginCommand("unknown-user", "1234");
        when(userPersistencePort.findByUsername("unknown-user")).thenReturn(Optional.empty());

        // When & Then
        UserException exception = assertThrows(UserException.class, () -> {
            loginService.login(command);
        });

        assertEquals(ErrorCode.UNAUTHORIZED_USER, exception.getErrorCode());
        verify(userPersistencePort).findByUsername("unknown-user");
        verifyNoInteractions(passwordEncoder, tokenPort);
    }

    @Test
    @DisplayName("비밀번호가 불일치하는 상태로 로그인 시도 시 UNAUTHORIZED_USER 예외가 발생한다")
    void loginFailBadCredentials() {
        // Given
        LoginCommand command = new LoginCommand("test-user", "wrong-password");
        User user = User.builder()
                .id(1L)
                .username("test-user")
                .password("encoded-1234")
                .role(Role.ADMIN)
                .build();

        when(userPersistencePort.findByUsername("test-user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-1234")).thenReturn(false);

        // When & Then
        UserException exception = assertThrows(UserException.class, () -> {
            loginService.login(command);
        });

        assertEquals(ErrorCode.UNAUTHORIZED_USER, exception.getErrorCode());
        verify(userPersistencePort).findByUsername("test-user");
        verify(passwordEncoder).matches("wrong-password", "encoded-1234");
        verifyNoInteractions(tokenPort, refreshTokenPort);
    }
}

package com.mcp.mcp_pilot.user.application.service;

import com.mcp.mcp_pilot.user.port.in.LogoutUseCase;
import com.mcp.mcp_pilot.user.port.in.dto.LogoutCommand;
import com.mcp.mcp_pilot.user.port.out.RefreshTokenPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final RefreshTokenPort refreshTokenPort;

    @Override
    public void logout(LogoutCommand command) {
        log.info("[LogoutService] 비즈니스 로그아웃 프로세스 가동. 토큰 파괴를 요청합니다.");
        
        if (command.refreshToken() == null || command.refreshToken().isBlank()) {
            log.warn("[LogoutService] 로그아웃 대상 Refresh Token이 누락되었습니다.");
            return;
        }

        refreshTokenPort.   revoke(command.refreshToken());
    }
}

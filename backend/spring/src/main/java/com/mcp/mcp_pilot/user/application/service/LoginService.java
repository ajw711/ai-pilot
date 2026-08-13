package com.mcp.mcp_pilot.user.application.service;

import com.mcp.mcp_pilot.common.exception.ErrorCode;
import com.mcp.mcp_pilot.user.domain.entitiy.User;
import com.mcp.mcp_pilot.user.exception.UserException;
import com.mcp.mcp_pilot.user.port.in.LoginUseCase;
import com.mcp.mcp_pilot.user.port.in.dto.LoginCommand;
import com.mcp.mcp_pilot.user.port.in.dto.LoginResult;
import com.mcp.mcp_pilot.user.port.out.RefreshTokenPort;
import com.mcp.mcp_pilot.user.port.out.UserPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import com.mcp.mcp_pilot.user.port.out.TokenPort;
import com.mcp.mcp_pilot.user.port.out.dto.TokenResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final UserPersistencePort userPersistencePort;
    private final RefreshTokenPort refreshTokenPort;
    private final TokenPort tokenPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    public TokenResult login(LoginCommand command) {
        log.info("[LoginService] 비즈니스 로그인 프로세스 가동. ");

        Optional<User> userOpt = userPersistencePort.findByUsername(command.username());
        if (userOpt.isEmpty()) {
            log.warn("[LoginService] 사용자를 찾을 수 없습니다. Username: {}", command.username());
            throw new UserException(ErrorCode.UNAUTHORIZED_USER);
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            log.warn("[LoginService] 패스워드가 일치하지 않습니다. Username: {}", command.username());
            throw new UserException(ErrorCode.UNAUTHORIZED_USER);
        }

        TokenResult token = tokenPort.generateTokens(user);
        refreshTokenPort.save(user.getId(), token.refreshToken(), LocalDateTime.now().plusDays(14));
        log.info("[LoginService] 로그인 성공 및 JWT 발급 완료. Username: {}", command.username());

        return new TokenResult(token.accessToken(), token.refreshToken());
    }
}

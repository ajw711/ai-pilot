package com.mcp.mcp_pilot.user.adapter.out.token;

import com.mcp.mcp_pilot.common.security.JwtProvider;
import com.mcp.mcp_pilot.user.domain.entitiy.User;
import com.mcp.mcp_pilot.user.port.out.dto.TokenResult;
import com.mcp.mcp_pilot.user.port.out.TokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.mcp.mcp_pilot.user.port.out.RefreshTokenPort;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class JwtTokenAdapter implements TokenPort {

    private final JwtProvider jwtProvider;
    private final RefreshTokenPort refreshTokenPort;

    @Override
    public TokenResult generateTokens(User user) {
        String accessToken = jwtProvider.generateAccessToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name()
        );

        String refreshToken = jwtProvider.generateRefreshToken(
                user.getId()
        );

        // Refresh Token DB 기록 (만료일자 14일)
        refreshTokenPort.save(user.getId(), refreshToken, LocalDateTime.now().plusDays(14));

        return new TokenResult(accessToken, refreshToken);
    }
}

package com.mcp.mcp_pilot.user.adapter.out.token;

import com.mcp.mcp_pilot.common.security.JwtProvider;
import com.mcp.mcp_pilot.user.domain.entitiy.User;
import com.mcp.mcp_pilot.user.port.out.dto.TokenResult;
import com.mcp.mcp_pilot.user.port.out.TokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenAdapter implements TokenPort {

    private final JwtProvider jwtProvider;

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

        return new TokenResult(accessToken, refreshToken);
    }
}

package com.mcp.mcp_pilot.user.application.service;

import com.mcp.mcp_pilot.common.exception.ErrorCode;
import com.mcp.mcp_pilot.user.adapter.out.persistence.entity.RefreshTokenJpaEntity;
import com.mcp.mcp_pilot.user.domain.entitiy.User;
import com.mcp.mcp_pilot.user.exception.UserException;
import com.mcp.mcp_pilot.user.port.in.TokenRefreshUseCase;
import com.mcp.mcp_pilot.user.port.out.RefreshTokenPort;
import com.mcp.mcp_pilot.user.port.out.TokenPort;
import com.mcp.mcp_pilot.user.port.out.UserPersistencePort;
import com.mcp.mcp_pilot.user.port.out.dto.TokenResult;
import com.mcp.mcp_pilot.user.port.out.dto.TokenRotationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRefreshService implements TokenRefreshUseCase {

    private final RefreshTokenPort refreshTokenPort;
    private final UserPersistencePort userPersistencePort;
    private final TokenPort tokenPort;

    @Override
    @Transactional
    public TokenRotationResult rotate(String refreshToken) {
        log.info("[TokenRefreshService] 토큰 로테이션 프로세스 기동.");

        // 기존 토큰 존재 및 유효성 확인
        RefreshTokenJpaEntity oldToken = refreshTokenPort.findByTokenHash(refreshToken)
                .orElseThrow(() -> new UserException(ErrorCode.UNAUTHORIZED_USER));

        if (oldToken.isRevoked() || oldToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            log.warn("[TokenRefreshService] 사용 불가 토큰으로 갱신 시도 차단.");
            throw new UserException(ErrorCode.UNAUTHORIZED_USER);
        }

        Long userId = oldToken.getUserId();
        LocalDateTime originalExpiredAt = oldToken.getExpiredAt(); // 최초 만료일 승계 보존

        // 도메인 엔티티 로딩
        User user = userPersistencePort.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.UNAUTHORIZED_USER));

        // 기존 토큰 즉시 무효화
        refreshTokenPort.revoke(refreshToken);

        // 신규 토큰 세트 생성
        TokenResult newTokens = tokenPort.generateTokens(user);

        // 최초 만료일을 계승하여 신규 리프레쉬 토큰 DB 기록
        refreshTokenPort.save(userId, newTokens.refreshToken(), originalExpiredAt);

        log.info("[TokenRefreshService] 로테이션 완료. UserID: {}", userId);

        return new TokenRotationResult(newTokens.accessToken(), newTokens.refreshToken(), originalExpiredAt);
    }
}

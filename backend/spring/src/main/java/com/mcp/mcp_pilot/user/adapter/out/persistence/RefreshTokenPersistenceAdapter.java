package com.mcp.mcp_pilot.user.adapter.out.persistence;

import com.mcp.mcp_pilot.user.adapter.out.persistence.entity.RefreshTokenJpaEntity;
import com.mcp.mcp_pilot.user.adapter.out.persistence.repository.RefreshTokenJpaRepository;
import com.mcp.mcp_pilot.user.port.out.RefreshTokenPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenPersistenceAdapter implements RefreshTokenPort {

    private final RefreshTokenJpaRepository repository;

    @Override
    @Transactional
    public void save(Long userId, String tokenHash, LocalDateTime expiredAt) {
        log.info("[RefreshTokenPersistenceAdapter] Refresh Token DB 기록 시작. UserID: {}", userId);
        RefreshTokenJpaEntity entity = RefreshTokenJpaEntity.create(userId, tokenHash, expiredAt);
        repository.save(entity);
    }

    @Override
    @Transactional
    public void revoke(String tokenHash) {
        log.info("[RefreshTokenPersistenceAdapter] Refresh Token 무효화(Revoke) 시작.");
        repository.findByTokenHash(tokenHash).ifPresent(entity -> {
            entity.revoke();
            log.info("[RefreshTokenPersistenceAdapter] Refresh Token 무효화 완료. DB ID: {}", entity.getId());
        });
    }
}

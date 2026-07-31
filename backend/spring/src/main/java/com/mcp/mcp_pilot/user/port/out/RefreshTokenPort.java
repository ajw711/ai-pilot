package com.mcp.mcp_pilot.user.port.out;

import com.mcp.mcp_pilot.user.adapter.out.persistence.entity.RefreshTokenJpaEntity;
import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenPort {
    void save(Long userId, String tokenHash, LocalDateTime expiredAt);
    void revoke(String tokenHash);
    Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);
}

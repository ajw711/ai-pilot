package com.mcp.mcp_pilot.user.adapter.out.persistence.repository;

import com.mcp.mcp_pilot.user.adapter.out.persistence.entity.RefreshTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, Long> {
    Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);
}

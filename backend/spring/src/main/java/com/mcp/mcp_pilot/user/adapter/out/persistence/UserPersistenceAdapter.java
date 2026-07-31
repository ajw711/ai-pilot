package com.mcp.mcp_pilot.user.adapter.out.persistence;

import com.mcp.mcp_pilot.user.adapter.out.persistence.entity.UserJpaEntity;
import com.mcp.mcp_pilot.user.adapter.out.persistence.repository.UserJpaRepository;

import com.mcp.mcp_pilot.user.domain.entitiy.User;
import com.mcp.mcp_pilot.user.port.out.UserPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPersistencePort {

    private final UserJpaRepository repository;

    @Override
    public Optional<User> findByUsername(String username) {
        log.info("[UserPersistenceAdapter] DB 사용자 조회 요청. Username: {}", username);
        Optional<UserJpaEntity> entityOpt = repository.findByUsername(username);
        return entityOpt.map(this::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        log.info("[UserPersistenceAdapter] DB 사용자 조회 요청. ID: {}", id);
        return repository.findById(id).map(this::toDomain);
    }

    private User toDomain(UserJpaEntity entity) {
        return User.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .password(entity.getPassword())
                .role(entity.getRole())
                .build();
    }
}

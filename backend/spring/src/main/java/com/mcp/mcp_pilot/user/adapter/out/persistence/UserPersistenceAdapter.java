package com.mcp.mcp_pilot.user.adapter.out.persistence;

import com.mcp.mcp_pilot.user.adapter.out.persistence.entity.UserJpaEntity;
import com.mcp.mcp_pilot.user.adapter.out.persistence.repository.UserJpaRepository;

import com.mcp.mcp_pilot.user.domain.entitiy.User;
import com.mcp.mcp_pilot.user.domain.vo.Role;
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

        // 로컬 데모 환경을 위한 최초 사용자 자동 Seed 처리
        if (entityOpt.isEmpty() && "test-user".equals(username)) {
            log.info("[UserPersistenceAdapter] 데모 계정(test-user)이 없어 자동 시딩을 가동합니다.");
            // "1234"에 대한 BCrypt 해싱 고정값 주입
            String encodedPassword = "$2a$10$8.K3.54y8/u2K4p.l6jHGe66z8f02L18O6oY83d1S8z3J6n9Gk1uG";
            UserJpaEntity demoEntity = UserJpaEntity.create("test-user", encodedPassword, Role.ADMIN);
            repository.save(demoEntity);
            return Optional.of(toDomain(demoEntity));
        }

        return entityOpt.map(this::toDomain);
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

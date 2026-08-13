package com.mcp.mcp_pilot.user.adapter.out.persistence.entity;

import com.mcp.mcp_pilot.common.entitiy.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@SQLRestriction("deleted_at IS NULL")
@Table(name = "refresh_token",
    indexes = {
        @Index(name = "idx_token_hash_unique", columnList = "token_hash", unique = true)
    }
)
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RefreshTokenJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 200)
    private String tokenHash;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(nullable = false)
    private boolean revoked;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private RefreshTokenJpaEntity(Long userId, String tokenHash, LocalDateTime expiredAt, boolean revoked) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiredAt = expiredAt;
        this.revoked = revoked;
    }

    public static RefreshTokenJpaEntity create(Long userId, String tokenHash, LocalDateTime expiredAt) {
        return new RefreshTokenJpaEntity(userId, tokenHash, expiredAt, false);
    }

    public void revoke() {
        this.revoked = true;
    }
}

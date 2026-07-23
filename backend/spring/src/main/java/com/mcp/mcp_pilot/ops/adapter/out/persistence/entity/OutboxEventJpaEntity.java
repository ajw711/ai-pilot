package com.mcp.mcp_pilot.ops.adapter.out.persistence.entity;

import com.mcp.mcp_pilot.common.entitiy.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "outbox_event")
@Entity
@Getter
@NoArgsConstructor
public class OutboxEventJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String eventId;

    @Column(nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private boolean published;

    @Column(nullable = false)
    private int retryCount;

    private OutboxEventJpaEntity(String eventId,
                                 String eventType,
                                 String payload,
                                 boolean published,
                                 int retryCount) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.payload = payload;
        this.published = published;
        this.retryCount = retryCount;
    }

    public static OutboxEventJpaEntity create(String eventId,
                                              String eventType,
                                              String payload,
                                              boolean published,
                                              int retryCount) {
        return new OutboxEventJpaEntity(
                eventId,
                eventType,
                payload,
                published,
                retryCount
        );
    }

    // 성공 상태를 명확히 정의
    public void markPublished() {
        this.published = true;
    }

    public void incrementRetry() {
        this.retryCount++;
    }
}
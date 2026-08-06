package com.mcp.mcp_pilot.common.outbox.repository;

import com.mcp.mcp_pilot.common.outbox.entity.OutboxEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, Long> {

    @Query("SELECT e FROM OutboxEventJpaEntity e WHERE e.published = false AND e.failed = false AND e.retryCount < :maxRetryCount")
    List<OutboxEventJpaEntity> findPendingEvents(@Param("maxRetryCount") int maxRetryCount);

    @Query(value = "SELECT * FROM outbox_event WHERE id = :id AND published = false AND failed = false FOR UPDATE SKIP LOCKED", nativeQuery = true)
    Optional<OutboxEventJpaEntity> findByIdForUpdateSkipLocked(@Param("id") Long id);
}

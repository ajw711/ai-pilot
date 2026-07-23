package com.mcp.mcp_pilot.ops.adapter.out.persistence.repository;

import com.mcp.mcp_pilot.ops.adapter.out.persistence.entity.OutboxEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, Long> {
    // 아직 발행되지 않은 아웃박스 이벤트 중 재시도 횟수가 1 미만인 건들만 조회
    List<OutboxEventJpaEntity> findByPublishedFalseAndRetryCountLessThan(int maxRetryCount);
}

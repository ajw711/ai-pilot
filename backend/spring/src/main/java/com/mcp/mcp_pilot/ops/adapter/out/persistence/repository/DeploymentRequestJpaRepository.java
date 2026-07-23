package com.mcp.mcp_pilot.ops.adapter.out.persistence.repository;

import com.mcp.mcp_pilot.ops.adapter.out.persistence.entity.DeploymentRequestJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeploymentRequestJpaRepository extends JpaRepository<DeploymentRequestJpaEntity, Long> {
    Optional<DeploymentRequestJpaEntity> findByTrackingId(String trackingId);
}
package com.mcp.mcp_pilot.document.adapter.out.persistence.repository;

import com.mcp.mcp_pilot.document.adapter.out.persistence.entity.DocumentFileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentFileJpaRepository extends JpaRepository<DocumentFileJpaEntity, Long> {
}

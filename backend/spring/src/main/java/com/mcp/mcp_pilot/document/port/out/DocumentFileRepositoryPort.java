package com.mcp.mcp_pilot.document.port.out;

import com.mcp.mcp_pilot.document.domain.DocumentFile;

import java.util.List;
import java.util.Optional;

public interface DocumentFileRepositoryPort {
    List<DocumentFile> saveAll(List<DocumentFile> documents);
    List<DocumentFile> findAll();
    Optional<DocumentFile> findById(Long id);
    List<DocumentFile> findAllById(List<Long> ids);
    void deleteAllById(List<Long> ids);
}

package com.mcp.mcp_pilot.document.port.out;

import com.mcp.mcp_pilot.document.domain.DocumentFile;

import java.util.List;

public interface DocumentFileRepositoryPort {
    List<DocumentFile> saveAll(List<DocumentFile> documents);
}

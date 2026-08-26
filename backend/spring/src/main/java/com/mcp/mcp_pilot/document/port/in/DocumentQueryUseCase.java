package com.mcp.mcp_pilot.document.port.in;

import com.mcp.mcp_pilot.document.port.in.dto.DocumentDownloadResult;
import com.mcp.mcp_pilot.document.port.in.dto.DocumentResult;

import java.util.List;

public interface DocumentQueryUseCase {
    List<DocumentResult> findAll();
    DocumentResult findById(Long id);
    DocumentDownloadResult download(Long id);
}

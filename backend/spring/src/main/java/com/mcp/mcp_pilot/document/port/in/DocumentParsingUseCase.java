package com.mcp.mcp_pilot.document.port.in;

import com.mcp.mcp_pilot.document.application.event.DocumentUploadedEvent;

public interface DocumentParsingUseCase {
    void execute(DocumentUploadedEvent event);
}

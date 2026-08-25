package com.mcp.mcp_pilot.document.port.in;

import com.mcp.mcp_pilot.document.port.in.dto.UploadDocumentCommand;
import com.mcp.mcp_pilot.document.port.in.dto.UploadDocumentResult;

public interface UploadDocumentUseCase {
    UploadDocumentResult uploadDocument(UploadDocumentCommand command);
}

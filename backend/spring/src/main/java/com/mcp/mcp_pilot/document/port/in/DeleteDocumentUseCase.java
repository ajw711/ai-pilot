package com.mcp.mcp_pilot.document.port.in;

import java.util.List;

public interface DeleteDocumentUseCase {
    void deleteDocument(List<Long> ids);
}

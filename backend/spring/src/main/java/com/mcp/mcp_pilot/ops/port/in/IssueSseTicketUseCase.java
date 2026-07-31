package com.mcp.mcp_pilot.ops.port.in;

import com.mcp.mcp_pilot.ops.adapter.in.web.dto.TicketResponse;
import com.mcp.mcp_pilot.ops.port.in.dto.IssueSseTicketCommand;

public interface IssueSseTicketUseCase {
    TicketResponse issue(IssueSseTicketCommand issueSseTicketCommand);
}

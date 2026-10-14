package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasTicketRegistryTools}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasTicketRegistryTools {
    private final TicketRegistry ticketRegistry;

    /**
     * Find registered ticket.
     *
     * @param ticketId the ticket id
     * @return the registered ticket
     */
    @McpTool(description = "Look up a ticket by its id")
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public @Nullable Ticket findTicket(
        @McpToolParam(description = "The id of the ticket to look up")
        final String ticketId) {
        return ticketRegistry.getTicket(ticketId);
    }
}

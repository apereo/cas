package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import lombok.RequiredArgsConstructor;
import lombok.val;
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
    private static final int MAX_SESSIONS = 50;

    private final TicketRegistry ticketRegistry;

    private final TicketSerializationManager ticketSerializationManager;

    /**
     * Find a ticket and return its JSON representation.
     *
     * @param ticketId the ticket id
     * @return the ticket, or null if it cannot be found or has expired
     */
    @McpTool(description = "Look up a ticket by its id in the ticket registry and return it as JSON",
        title = "Find Ticket",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public @Nullable String findTicket(
        @McpToolParam(description = "The id of the ticket to look up")
        final String ticketId) {
        val ticket = ticketRegistry.getTicket(ticketId);
        return ticket != null ? ticketSerializationManager.serializeTicket(ticket) : null;
    }

    /**
     * Find the active single sign-on sessions of a person.
     *
     * @param username the username
     * @return the number of sessions and a summary of each
     */
    @McpTool(description = "Find the active single sign-on sessions (ticket-granting tickets) of a person, "
        + "with when each was created and last used, the authentication handlers and the applications accessed. "
        + "At most " + MAX_SESSIONS + " sessions are summarized; use findTicket with a session id for its full details.",
        title = "Find SSO Sessions",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> findSessions(
        @McpToolParam(description = "The username (principal id) of the person")
        final String username) {
        try (val sessions = ticketRegistry.getSessionsFor(username)) {
            val summaries = sessions
                .filter(TicketGrantingTicket.class::isInstance)
                .map(TicketGrantingTicket.class::cast)
                .sorted(Comparator.comparing(TicketGrantingTicket::getCreationTime, Comparator.nullsLast(Comparator.<ZonedDateTime>reverseOrder())))
                .limit(MAX_SESSIONS)
                .map(CasTicketRegistryTools::summarize)
                .toList();
            val result = new LinkedHashMap<String, Object>();
            result.put("username", username);
            result.put("count", ticketRegistry.countSessionsFor(username));
            result.put("sessions", summaries);
            return result;
        }
    }

    /**
     * Find active single sign-on sessions whose attributes contain a value.
     *
     * @param attributeName  the attribute name
     * @param attributeValue the attribute value
     * @return the matching sessions
     */
    @McpTool(description = "Find active single sign-on sessions (ticket-granting tickets) whose principal or authentication "
        + "attributes contain the given value, ignoring case, such as everyone logged in from a given department. "
        + "At most " + MAX_SESSIONS + " sessions are summarized.",
        title = "Find SSO Sessions By Attribute",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> findSessionsWithAttribute(
        @McpToolParam(description = "The name of the principal or authentication attribute")
        final String attributeName,
        @McpToolParam(description = "The attribute value to look for")
        final String attributeValue) {
        try (val sessions = ticketRegistry.getSessionsWithAttributes(Map.of(attributeName, List.of(attributeValue)))) {
            val summaries = sessions
                .filter(TicketGrantingTicket.class::isInstance)
                .map(TicketGrantingTicket.class::cast)
                .limit(MAX_SESSIONS)
                .map(CasTicketRegistryTools::summarize)
                .toList();
            val result = new LinkedHashMap<String, Object>();
            result.put("attributeName", attributeName);
            result.put("attributeValue", attributeValue);
            result.put("count", summaries.size());
            result.put("sessions", summaries);
            return result;
        }
    }

    private static Map<String, Object> summarize(final TicketGrantingTicket ticketGrantingTicket) {
        val authentication = ticketGrantingTicket.getAuthentication();
        val summary = new LinkedHashMap<String, Object>();
        summary.put("id", ticketGrantingTicket.getId());
        summary.put("creationTime", String.valueOf(ticketGrantingTicket.getCreationTime()));
        summary.put("lastTimeUsed", String.valueOf(ticketGrantingTicket.getLastTimeUsed()));
        summary.put("countOfUses", ticketGrantingTicket.getCountOfUses());
        if (authentication != null) {
            summary.put("principal", authentication.getPrincipal().getId());
            summary.put("authenticationDate", String.valueOf(authentication.getAuthenticationDate()));
            summary.put("authenticationHandlers", new TreeSet<>(authentication.getSuccesses().keySet()));
        }
        summary.put("services", ticketGrantingTicket.getServices().values()
            .stream()
            .map(Service::getId)
            .collect(Collectors.toCollection(TreeSet::new)));
        return summary;
    }
}

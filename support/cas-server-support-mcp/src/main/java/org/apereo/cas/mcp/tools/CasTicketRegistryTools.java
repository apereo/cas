package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.mcp.CasMcpSessionReferences;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.PropertiesAwareTicket;
import org.apereo.cas.ticket.RenewableServiceTicket;
import org.apereo.cas.ticket.ServiceAwareTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketAwareTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.aop.support.AopUtils;
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

    /**
     * Find a ticket and summarize it. Attribute values and the serialized ticket are never returned.
     *
     * @param ticketId the ticket id, or a session reference returned by findSessions
     * @param username the username of the person, needed when the ticket id is a session reference
     * @return the ticket summary, or a reason when it cannot be found or has expired
     */
    @McpTool(description = "Look up a ticket by its id in the ticket registry and summarize it: its type, when it was "
        + "created and last used, how often it was used, its expiration policy, the principal and the names of the "
        + "principal and authentication attributes, the application of a service ticket, the parent ticket, and for a "
        + "single sign-on session the applications accessed. Accepts a ticket id, or a session reference returned by "
        + "findSessions together with the person's username. Single sign-on session ids and attribute values are never "
        + "returned; use findPerson or previewAttributeRelease for attribute values.",
        title = "Find Ticket",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> findTicket(
        @McpToolParam(description = "The id of the ticket to look up, or a session reference returned by findSessions")
        final String ticketId,
        @McpToolParam(description = "The username of the person, required when ticketId is a session reference", required = false)
        final @Nullable String username) {
        val result = new LinkedHashMap<String, Object>();
        result.put("ticketId", ticketId);
        val found = CasMcpSessionReferences.resolve(ticketRegistry, ticketId, username);
        if (found.isEmpty()) {
            result.put("reason", CasMcpSessionReferences.isReference(ticketId) && StringUtils.isBlank(username)
                ? "A session reference can only be looked up with the username of the person"
                : "No ticket is found for " + ticketId + "; it may have expired");
            return result;
        }
        val ticket = found.get();
        result.put("type", AopUtils.getTargetClass(ticket).getSimpleName());
        result.put("prefix", ticket.getPrefix());
        result.put("tenantId", ticket.getTenantId());
        result.put("creationTime", String.valueOf(ticket.getCreationTime()));
        result.put("lastTimeUsed", String.valueOf(ticket.getLastTimeUsed()));
        result.put("countOfUses", ticket.getCountOfUses());
        result.put("expired", ticket.isExpired());
        val expirationPolicy = ticket.getExpirationPolicy();
        if (expirationPolicy != null) {
            result.put("expirationPolicy", expirationPolicy.getName());
            result.put("timeToLive", expirationPolicy.getTimeToLive(ticket));
        }
        if (ticket instanceof final AuthenticationAwareTicket authenticationAware && authenticationAware.getAuthentication() != null) {
            result.put("authentication", summarize(Objects.requireNonNull(authenticationAware.getAuthentication())));
        }
        if (ticket instanceof final ServiceAwareTicket serviceAware && serviceAware.getService() != null) {
            result.put("service", serviceAware.getService().getId());
        }
        if (ticket instanceof final RenewableServiceTicket renewable) {
            result.put("fromNewLogin", renewable.isFromNewLogin());
        }
        if (ticket instanceof final TicketGrantingTicket ticketGrantingTicket) {
            result.put("sessionReference", CasMcpSessionReferences.of(ticketGrantingTicket.getId()));
            result.put("root", ticketGrantingTicket.isRoot());
            result.put("services", ticketGrantingTicket.getServices().values()
                .stream()
                .map(Service::getId)
                .collect(Collectors.toCollection(TreeSet::new)));
            val proxiedBy = ticketGrantingTicket.getProxiedBy();
            result.put("proxiedBy", proxiedBy != null ? proxiedBy.getId() : null);
            result.put("descendantTickets", ticketGrantingTicket.getDescendantTickets().size());
        } else if (ticket instanceof final TicketGrantingTicketAwareTicket parentAware && parentAware.getTicketGrantingTicket() != null) {
            result.put("sessionReference", CasMcpSessionReferences.of(Objects.requireNonNull(parentAware.getTicketGrantingTicket()).getId()));
        }
        if (ticket instanceof final PropertiesAwareTicket propertiesAware) {
            result.put("propertyNames", new TreeSet<>(propertiesAware.getProperties().keySet()));
        }
        return result;
    }

    /**
     * Find the active single sign-on sessions of a person.
     *
     * @param username the username
     * @return the number of sessions and a summary of each
     */
    @McpTool(description = "Find the active single sign-on sessions (ticket-granting tickets) of a person, "
        + "with when each was created and last used, the authentication handlers and the applications accessed. "
        + "At most " + MAX_SESSIONS + " sessions are summarized; use findTicket with a session reference and the username for more details.",
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

    private static Map<String, Object> summarize(final Authentication authentication) {
        val summary = new LinkedHashMap<String, Object>();
        summary.put("principal", authentication.getPrincipal().getId());
        summary.put("authenticationDate", String.valueOf(authentication.getAuthenticationDate()));
        summary.put("authenticationHandlers", new TreeSet<>(authentication.getSuccesses().keySet()));
        summary.put("principalAttributeNames", new TreeSet<>(authentication.getPrincipal().getAttributes().keySet()));
        summary.put("authenticationAttributeNames", new TreeSet<>(authentication.getAttributes().keySet()));
        return summary;
    }

    private static Map<String, Object> summarize(final TicketGrantingTicket ticketGrantingTicket) {
        val authentication = ticketGrantingTicket.getAuthentication();
        val summary = new LinkedHashMap<String, Object>();
        summary.put("sessionReference", CasMcpSessionReferences.of(ticketGrantingTicket.getId()));
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

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
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
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
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public TicketSummary findTicket(
        @McpToolParam(description = "The id of the ticket to look up, or a session reference returned by findSessions")
        final String ticketId,
        @McpToolParam(description = "The username of the person, required when ticketId is a session reference", required = false)
        final @Nullable String username) {
        val found = CasMcpSessionReferences.resolve(ticketRegistry, ticketId, username);
        if (found.isEmpty()) {
            return TicketSummary.builder()
                .ticketId(ticketId)
                .reason(CasMcpSessionReferences.isReference(ticketId) && StringUtils.isBlank(username)
                    ? "A session reference can only be looked up with the username of the person"
                    : "No ticket is found for " + ticketId + "; it may have expired")
                .build();
        }
        val ticket = found.get();
        val summary = TicketSummary.builder()
            .ticketId(ticketId)
            .type(AopUtils.getTargetClass(ticket).getSimpleName())
            .prefix(ticket.getPrefix())
            .tenantId(ticket.getTenantId())
            .creationTime(String.valueOf(ticket.getCreationTime()))
            .lastTimeUsed(String.valueOf(ticket.getLastTimeUsed()))
            .countOfUses(ticket.getCountOfUses())
            .expired(ticket.isExpired());
        val expirationPolicy = ticket.getExpirationPolicy();
        if (expirationPolicy != null) {
            summary.expirationPolicy(expirationPolicy.getName()).timeToLive(expirationPolicy.getTimeToLive(ticket));
        }
        if (ticket instanceof final AuthenticationAwareTicket authenticationAware && authenticationAware.getAuthentication() != null) {
            summary.authentication(summarize(Objects.requireNonNull(authenticationAware.getAuthentication())));
        }
        if (ticket instanceof final ServiceAwareTicket serviceAware && serviceAware.getService() != null) {
            summary.service(serviceAware.getService().getId());
        }
        if (ticket instanceof final RenewableServiceTicket renewable) {
            summary.fromNewLogin(renewable.isFromNewLogin());
        }
        if (ticket instanceof final TicketGrantingTicket ticketGrantingTicket) {
            val proxiedBy = ticketGrantingTicket.getProxiedBy();
            summary.sessionReference(CasMcpSessionReferences.of(ticketGrantingTicket.getId()))
                .root(ticketGrantingTicket.isRoot())
                .services(serviceIds(ticketGrantingTicket))
                .proxiedBy(proxiedBy != null ? proxiedBy.getId() : null)
                .descendantTickets(ticketGrantingTicket.getDescendantTickets().size());
        } else if (ticket instanceof final TicketGrantingTicketAwareTicket parentAware && parentAware.getTicketGrantingTicket() != null) {
            summary.sessionReference(CasMcpSessionReferences.of(Objects.requireNonNull(parentAware.getTicketGrantingTicket()).getId()));
        }
        if (ticket instanceof final PropertiesAwareTicket propertiesAware) {
            summary.propertyNames(new TreeSet<>(propertiesAware.getProperties().keySet()).stream().toList());
        }
        return summary.build();
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
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public UserSessions findSessions(
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
            return new UserSessions(username, ticketRegistry.countSessionsFor(username), summaries);
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
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public AttributeSessions findSessionsWithAttribute(
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
            return new AttributeSessions(attributeName, attributeValue, summaries.size(), summaries);
        }
    }

    private static AuthenticationSummary summarize(final Authentication authentication) {
        return new AuthenticationSummary(authentication.getPrincipal().getId(), String.valueOf(authentication.getAuthenticationDate()),
            new TreeSet<>(authentication.getSuccesses().keySet()).stream().toList(),
            new TreeSet<>(authentication.getPrincipal().getAttributes().keySet()).stream().toList(),
            new TreeSet<>(authentication.getAttributes().keySet()).stream().toList());
    }

    private static SessionSummary summarize(final TicketGrantingTicket ticketGrantingTicket) {
        val authentication = ticketGrantingTicket.getAuthentication();
        return new SessionSummary(CasMcpSessionReferences.of(ticketGrantingTicket.getId()),
            String.valueOf(ticketGrantingTicket.getCreationTime()), String.valueOf(ticketGrantingTicket.getLastTimeUsed()),
            ticketGrantingTicket.getCountOfUses(),
            authentication != null ? authentication.getPrincipal().getId() : null,
            authentication != null ? String.valueOf(authentication.getAuthenticationDate()) : null,
            authentication != null ? new TreeSet<>(authentication.getSuccesses().keySet()).stream().toList() : null,
            serviceIds(ticketGrantingTicket));
    }

    private static List<String> serviceIds(final TicketGrantingTicket ticketGrantingTicket) {
        return ticketGrantingTicket.getServices().values()
            .stream()
            .map(Service::getId)
            .distinct()
            .sorted()
            .toList();
    }

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TicketSummary(String ticketId, @Nullable String type, @Nullable String prefix, @Nullable String tenantId,
                                @Nullable String creationTime, @Nullable String lastTimeUsed, @Nullable Integer countOfUses,
                                @Nullable Boolean expired, @Nullable String expirationPolicy, @Nullable Long timeToLive,
                                @Nullable AuthenticationSummary authentication, @Nullable String service,
                                @Nullable Boolean fromNewLogin, @Nullable String sessionReference, @Nullable Boolean root,
                                @Nullable List<String> services, @Nullable String proxiedBy, @Nullable Integer descendantTickets,
                                @Nullable List<String> propertyNames, @Nullable String reason) {
    }

    public record AuthenticationSummary(String principal, String authenticationDate, List<String> authenticationHandlers,
                                        List<String> principalAttributeNames, List<String> authenticationAttributeNames) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SessionSummary(String sessionReference, String creationTime, String lastTimeUsed, int countOfUses,
                                 @Nullable String principal, @Nullable String authenticationDate,
                                 @Nullable List<String> authenticationHandlers, List<String> services) {
    }

    public record UserSessions(String username, long count, List<SessionSummary> sessions) {
    }

    public record AttributeSessions(String attributeName, String attributeValue, int count, List<SessionSummary> sessions) {
    }
}

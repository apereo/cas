package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.mcp.CasMcpSessionReferences;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasSingleLogoutTools}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasSingleLogoutTools {
    private final TicketRegistry ticketRegistry;

    private final ServicesManager servicesManager;

    private final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder;

    private final CasConfigurationProperties casProperties;

    /**
     * Preview which applications would be notified if a single sign-on session ended.
     *
     * @param ticketId the ticket-granting ticket id, or a session reference returned by findSessions
     * @param username the username of the person, needed when the ticket id is a session reference
     * @return the applications and how each would be notified
     */
    @McpTool(description = "Preview which applications would receive a single logout notification if a single sign-on "
        + "session (ticket-granting ticket) ended now, with the logout URL and channel (back or front channel) of each. "
        + "Accepts the ticket-granting ticket id, or a session reference returned by findSessions together with the "
        + "person's username. Nothing is logged out.",
        title = "Preview Single Logout",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> previewLogout(
        @McpToolParam(description = "The ticket-granting ticket id, or a session reference returned by findSessions")
        final String ticketId,
        @McpToolParam(description = "The username of the person, required when ticketId is a session reference", required = false)
        final @Nullable String username) {
        val result = new LinkedHashMap<String, Object>();
        result.put("ticketId", ticketId);
        result.put("singleLogoutDisabled", casProperties.getSlo().isDisabled());
        val ticket = CasMcpSessionReferences.resolve(ticketRegistry, ticketId, username).orElse(null);
        if (!(ticket instanceof final TicketGrantingTicket ticketGrantingTicket)) {
            result.put("reason", "No active single sign-on session matches " + ticketId);
            return result;
        }
        val applications = ticketGrantingTicket.getServices().values()
            .stream()
            .filter(WebApplicationService.class::isInstance)
            .map(WebApplicationService.class::cast)
            .collect(Collectors.toMap(WebApplicationService::getId, Function.identity(), (first, second) -> first, TreeMap::new))
            .values()
            .stream()
            .map(this::describe)
            .toList();
        result.put("count", applications.size());
        result.put("applications", applications);
        return result;
    }

    private Map<String, Object> describe(final WebApplicationService service) {
        val application = new LinkedHashMap<String, Object>();
        application.put("service", service.getId());
        val registeredService = servicesManager.findServiceBy(service);
        if (registeredService == null) {
            application.put("notified", false);
            application.put("reason", "No registered service matches this application");
            return application;
        }
        application.put("registeredService", registeredService.getName());
        val logoutUrls = singleLogoutServiceLogoutUrlBuilder.determineLogoutUrl(registeredService, service)
            .stream()
            .filter(url -> url.getLogoutType() != RegisteredServiceLogoutType.NONE)
            .map(url -> Map.<String, Object>of("url", url.getUrl(), "logoutType", String.valueOf(url.getLogoutType())))
            .toList();
        val sloDisabled = casProperties.getSlo().isDisabled();
        application.put("notified", !sloDisabled && !logoutUrls.isEmpty());
        application.put("logoutUrls", logoutUrls);
        if (logoutUrls.isEmpty()) {
            application.put("reason", "Single logout is turned off for this application or it has no valid logout URL");
        }
        return application;
    }
}

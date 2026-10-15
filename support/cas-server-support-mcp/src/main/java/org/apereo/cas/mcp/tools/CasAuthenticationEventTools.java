package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.ClassUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasAuthenticationEventTools}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasAuthenticationEventTools {
    private static final int MAX_EVENTS = 100;

    private static final int DEFAULT_DAYS = 7;

    private final ObjectProvider<CasEventRepository> casEventRepository;

    /**
     * Find the most recent authentication events of a person. Device fingerprints are never returned.
     *
     * @param username the username
     * @param days     how many days to look back
     * @return the matching events, newest first
     */
    @McpTool(description = "Find the recorded authentication events of a person, newest first, such as single sign-on "
        + "sessions created and destroyed, authentication failures and risky authentication attempts, with the client IP "
        + "address, user agent and location of each. Events are only recorded when an events repository is configured. "
        + "At most " + MAX_EVENTS + " events are returned.",
        title = "Find Authentication Events",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public AuthenticationEvents findAuthenticationEvents(
        @McpToolParam(description = "The username (principal id) of the person")
        final String username,
        @McpToolParam(description = "How many days to look back; defaults to " + DEFAULT_DAYS, required = false)
        final @Nullable Integer days) {
        val lookBack = days != null && days > 0 ? days : DEFAULT_DAYS;
        val since = ZonedDateTime.now(Clock.systemUTC()).minusDays(lookBack);
        val repository = casEventRepository.getIfAvailable();
        if (repository == null || BeanSupplier.isProxy(repository)) {
            return new AuthenticationEvents(username, since.toString(), 0, List.of(), "Authentication events are not recorded");
        }
        try (val stream = repository.getEventsForPrincipal(username, since)) {
            val events = stream
                .filter(event -> event.getCreationTime() != null && event.getCreationTime().isAfter(since.toInstant()))
                .sorted(Comparator.comparing(CasEvent::getCreationTime).reversed())
                .limit(MAX_EVENTS)
                .map(CasAuthenticationEventTools::summarize)
                .toList();
            return new AuthenticationEvents(username, since.toString(), events.size(), events, null);
        }
    }

    private static AuthenticationEvent summarize(final CasEvent event) {
        return new AuthenticationEvent(event.getCreationTime().toString(), ClassUtils.getShortClassName(event.getType()),
            event.getEventId(), event.getClientIpAddress(), event.getServerIpAddress(), event.getAgent(),
            event.get(CasEvent.FIELD_GEO_ADDRESS), event.get(CasEvent.FIELD_GEO_LATITUDE), event.get(CasEvent.FIELD_GEO_LONGITUDE),
            event.getTenant());
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AuthenticationEvents(String username, String since, int count, List<AuthenticationEvent> events,
                                       @Nullable String reason) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AuthenticationEvent(String when, String type, @Nullable String eventId, @Nullable String clientIpAddress,
                                      @Nullable String serverIpAddress, @Nullable String userAgent, @Nullable String geoAddress,
                                      @Nullable String geoLatitude, @Nullable String geoLongitude, @Nullable String tenant) {
    }
}

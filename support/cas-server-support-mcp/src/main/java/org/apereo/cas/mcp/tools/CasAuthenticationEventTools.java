package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.util.spring.beans.BeanSupplier;
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
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> findAuthenticationEvents(
        @McpToolParam(description = "The username (principal id) of the person")
        final String username,
        @McpToolParam(description = "How many days to look back; defaults to " + DEFAULT_DAYS, required = false)
        final @Nullable Integer days) {
        val lookBack = days != null && days > 0 ? days : DEFAULT_DAYS;
        val since = ZonedDateTime.now(Clock.systemUTC()).minusDays(lookBack);
        val result = new LinkedHashMap<String, Object>();
        result.put("username", username);
        result.put("since", since.toString());
        val repository = casEventRepository.getIfAvailable();
        if (repository == null || BeanSupplier.isProxy(repository)) {
            result.put("reason", "Authentication events are not recorded");
            return result;
        }
        try (val stream = repository.getEventsForPrincipal(username, since)) {
            val events = stream
                .filter(event -> event.getCreationTime() != null && event.getCreationTime().isAfter(since.toInstant()))
                .sorted(Comparator.comparing(CasEvent::getCreationTime).reversed())
                .limit(MAX_EVENTS)
                .map(CasAuthenticationEventTools::summarize)
                .toList();
            result.put("count", events.size());
            result.put("events", events);
        }
        return result;
    }

    private static Map<String, Object> summarize(final CasEvent event) {
        val summary = new LinkedHashMap<String, Object>();
        summary.put("when", event.getCreationTime().toString());
        summary.put("type", ClassUtils.getShortClassName(event.getType()));
        summary.put("eventId", event.getEventId());
        summary.put("clientIpAddress", event.getClientIpAddress());
        summary.put("serverIpAddress", event.getServerIpAddress());
        summary.put("userAgent", event.getAgent());
        summary.put("geoAddress", event.get(CasEvent.FIELD_GEO_ADDRESS));
        summary.put("geoLatitude", event.get(CasEvent.FIELD_GEO_LATITUDE));
        summary.put("geoLongitude", event.get(CasEvent.FIELD_GEO_LONGITUDE));
        summary.put("tenant", event.getTenant());
        return summary;
    }
}

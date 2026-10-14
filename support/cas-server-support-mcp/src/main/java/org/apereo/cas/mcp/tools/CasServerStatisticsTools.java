package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasServerStatisticsTools}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasServerStatisticsTools {
    private final TicketRegistry ticketRegistry;

    private final ServicesManager servicesManager;

    /**
     * Report ticket and registered service counts.
     *
     * @return the counts
     */
    @McpTool(description = "Report how many single sign-on sessions, service tickets and tickets in total the ticket "
        + "registry holds, and how many registered services are loaded. Counts the registry cannot provide are omitted. "
        + "Counting may scan the whole ticket registry.",
        title = "Get Registry Statistics",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> getRegistryStatistics() {
        val result = new LinkedHashMap<String, Object>();
        result.put("ticketRegistry", ticketRegistry.getClass().getSimpleName());
        putCount(result, "singleSignOnSessions", ticketRegistry::sessionCount);
        putCount(result, "serviceTickets", ticketRegistry::serviceTicketCount);
        putCount(result, "tickets", ticketRegistry::countTickets);
        putCount(result, "registeredServices", servicesManager::count);
        return result;
    }

    private static void putCount(final Map<String, Object> result, final String name, final Supplier<@Nullable Long> counter) {
        try {
            val count = counter.get();
            if (count != null && count >= 0) {
                result.put(name, count);
            }
        } catch (final Throwable e) {
            result.put(name + "Error", Objects.requireNonNullElseGet(e.getMessage(), () -> e.getClass().getSimpleName()));
        }
    }
}

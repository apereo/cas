package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import com.fasterxml.jackson.annotation.JsonInclude;
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
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public RegistryStatistics getRegistryStatistics() {
        val errors = new LinkedHashMap<String, String>();
        return new RegistryStatistics(CasDeploymentTools.typeOf(ticketRegistry),
            count("singleSignOnSessions", ticketRegistry::sessionCount, errors),
            count("serviceTickets", ticketRegistry::serviceTicketCount, errors),
            count("tickets", ticketRegistry::countTickets, errors),
            count("registeredServices", servicesManager::count, errors),
            errors);
    }

    private static @Nullable Long count(final String name, final Supplier<@Nullable Long> counter, final Map<String, String> errors) {
        try {
            val count = counter.get();
            return count != null && count >= 0 ? count : null;
        } catch (final Throwable e) {
            errors.put(name, Objects.requireNonNullElseGet(e.getMessage(), () -> e.getClass().getSimpleName()));
            return null;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RegistryStatistics(String ticketRegistry, @Nullable Long singleSignOnSessions, @Nullable Long serviceTickets,
                                     @Nullable Long tickets, @Nullable Long registeredServices, Map<String, String> errors) {
    }
}

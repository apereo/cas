package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.serialization.StringSerializer;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasServiceManagementTools}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasServiceManagementTools {
    private static final int MAX_SERVICES = 50;

    private final ServicesManager servicesManager;

    private final ServiceFactory<WebApplicationService> serviceFactory;

    private final StringSerializer<RegisteredService> registeredServiceSerializer;

    /**
     * Find a registered service and return its JSON definition.
     *
     * @param serviceId the service id
     * @return the registered service definition, or null if none is found
     */
    @McpTool(description = "Look up a registered service (application) by its numeric id and return its definition as JSON",
        title = "Find Registered Service",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public @Nullable String findService(
        @McpToolParam(description = "The numeric id of the registered service to look up")
        final long serviceId) {
        val registeredService = servicesManager.findServiceBy(serviceId);
        return registeredService != null ? registeredServiceSerializer.toString(registeredService) : null;
    }

    /**
     * Find the registered service that matches an application URL and return its JSON definition.
     *
     * @param service the application URL
     * @return the matching registered service definition, or null if no registration matches
     */
    @McpTool(description = "Find the registered service (application) that matches an application URL, "
        + "the way CAS does during login, and return its definition as JSON",
        title = "Match Registered Service",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public @Nullable String matchService(
        @McpToolParam(description = "The application URL, such as the service parameter sent to CAS at login")
        final String service) {
        val webApplicationService = Objects.requireNonNull(serviceFactory.createService(service),
            () -> "Unable to build a service for " + service);
        val registeredService = servicesManager.findServiceBy(webApplicationService);
        return registeredService != null ? registeredServiceSerializer.toString(registeredService) : null;
    }

    /**
     * Search registered services by name, service id pattern, description or numeric id.
     *
     * @param query the search text
     * @return the number of matches and a summary of each
     */
    @McpTool(description = "Search registered services (applications) whose name, service id pattern, description or "
        + "numeric id contains the given text, ignoring case. At most " + MAX_SERVICES + " summaries are returned; "
        + "use findService with an id for the full definition.",
        title = "Search Registered Services",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> searchServices(
        @McpToolParam(description = "The text to look for; an empty value matches every registered service")
        final String query) {
        val term = StringUtils.trimToEmpty(query);
        val matches = servicesManager.getAllServices()
            .stream()
            .filter(registeredService -> term.isEmpty()
                || String.valueOf(registeredService.getId()).equals(term)
                || Strings.CI.contains(registeredService.getName(), term)
                || Strings.CI.contains(registeredService.getServiceId(), term)
                || Strings.CI.contains(registeredService.getDescription(), term))
            .toList();
        val result = new LinkedHashMap<String, Object>();
        result.put("query", term);
        result.put("count", matches.size());
        result.put("services", matches.stream().limit(MAX_SERVICES).map(CasServiceManagementTools::summarize).toList());
        return result;
    }

    private static Map<String, Object> summarize(final RegisteredService registeredService) {
        val summary = new LinkedHashMap<String, Object>();
        summary.put("id", registeredService.getId());
        summary.put("name", registeredService.getName());
        summary.put("serviceId", registeredService.getServiceId());
        summary.put("type", registeredService.getFriendlyName());
        summary.put("evaluationOrder", registeredService.getEvaluationOrder());
        return summary;
    }
}

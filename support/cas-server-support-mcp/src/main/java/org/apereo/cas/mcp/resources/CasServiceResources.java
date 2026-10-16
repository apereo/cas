package org.apereo.cas.mcp.resources;

import module java.base;
import org.apereo.cas.mcp.tools.CasServiceManagementTools;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import com.google.common.primitives.Longs;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.ai.mcp.annotation.McpComplete;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import tools.jackson.databind.json.JsonMapper;

/**
 * This is {@link CasServiceResources}. It offers registered services as an MCP resource template, with the same secret
 * redaction as the findService tool, and completes service ids from a typed id or part of a service name.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasServiceResources {
    /**
     * The URI template of a registered service.
     */
    public static final String SERVICE_URI_TEMPLATE = "cas://services/{id}";

    private static final int MAX_COMPLETIONS = 100;

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private final ServicesManager servicesManager;

    private final ObjectProvider<CasServiceManagementTools> serviceManagementTools;

    /**
     * Describe a registered service.
     *
     * @param id the numeric id of the registered service
     * @return the registered service definition as JSON, with secrets redacted
     */
    @McpResource(uri = SERVICE_URI_TEMPLATE, name = "casRegisteredService", title = "CAS Registered Service",
        description = "The definition of a registered service (application) by its numeric id, as JSON. Client secrets, "
            + "passwords, keys and inline key sets are replaced with <redacted>.",
        mimeType = "application/json")
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public String describeService(final String id) {
        val serviceId = Longs.tryParse(StringUtils.trimToEmpty(id));
        if (serviceId == null) {
            return MAPPER.writeValueAsString(Map.of("id", id, "reason", "A registered service id is a number"));
        }
        val definition = serviceManagementTools.getObject().findService(serviceId);
        return definition != null
            ? definition
            : MAPPER.writeValueAsString(Map.of("id", id, "reason", "No registered service has id " + id));
    }

    /**
     * Complete registered service ids.
     *
     * @param value the id typed so far, or part of a service name
     * @return the matching service ids
     */
    @McpComplete(uri = SERVICE_URI_TEMPLATE)
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public McpSchema.CompleteResult.CompleteCompletion completeServiceId(final String value) {
        val term = StringUtils.trimToEmpty(value);
        val matches = servicesManager.getAllServices()
            .stream()
            .filter(service -> term.isEmpty()
                || String.valueOf(service.getId()).startsWith(term)
                || Strings.CI.contains(service.getName(), term))
            .sorted(Comparator.comparingLong(RegisteredService::getId))
            .map(service -> String.valueOf(service.getId()))
            .toList();
        return new McpSchema.CompleteResult.CompleteCompletion(matches.stream().limit(MAX_COMPLETIONS).toList(),
            matches.size(), matches.size() > MAX_COMPLETIONS);
    }
}

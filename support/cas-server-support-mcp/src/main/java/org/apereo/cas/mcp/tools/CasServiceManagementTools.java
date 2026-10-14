package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import lombok.RequiredArgsConstructor;
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
    private final ServicesManager servicesManager;

    /**
     * Find registered service.
     *
     * @param serviceId the service id
     * @return the registered service
     */
    @McpTool(description = "Look up a service by its numeric id")
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public @Nullable RegisteredService findService(
        @McpToolParam(description = "The numeric id of the service to look up")
        final long serviceId) {
        return servicesManager.findServiceBy(serviceId);
    }
}

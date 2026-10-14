package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantsManager;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasTenantTools}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasTenantTools {
    private final ObjectProvider<TenantsManager> tenantsManager;

    /**
     * Find a tenant definition.
     *
     * @param tenantId the tenant id
     * @return the tenant definition, or the ids of the known tenants when it cannot be found
     */
    @McpTool(description = "Look up a tenant in a multitenant CAS deployment: its description, its authentication, "
        + "user interface and delegated authentication policies, and the names of the settings it overrides. "
        + "Setting values are never returned. When the tenant cannot be found, the ids of the known tenants are listed.",
        title = "Find Tenant",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public Map<String, Object> findTenant(
        @McpToolParam(description = "The id of the tenant")
        final String tenantId) {
        val result = new LinkedHashMap<String, Object>();
        result.put("tenantId", tenantId);
        val manager = tenantsManager.getIfAvailable();
        if (manager == null) {
            result.put("reason", "Multitenancy is not configured");
            return result;
        }
        manager.findTenant(tenantId).ifPresentOrElse(tenant -> result.put("tenant", summarize(tenant)), () -> {
            result.put("reason", "No tenant matches " + tenantId);
            result.put("knownTenants", manager.findTenants().stream().map(TenantDefinition::getId).sorted().toList());
        });
        return result;
    }

    private static Map<String, Object> summarize(final TenantDefinition tenant) {
        val summary = new LinkedHashMap<String, Object>();
        summary.put("id", tenant.getId());
        summary.put("description", tenant.getDescription());
        summary.put("overriddenSettings", new TreeSet<>(tenant.getProperties().keySet()));
        summary.put("authenticationPolicy", tenant.getAuthenticationPolicy());
        summary.put("userInterfacePolicy", tenant.getUserInterfacePolicy());
        summary.put("delegatedAuthenticationPolicy", tenant.getDelegatedAuthenticationPolicy());
        return summary;
    }
}

package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantsManager;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
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
     * Find a tenant.
     *
     * @param tenantId the tenant id
     * @return the tenant, or the known tenant ids when it cannot be found
     */
    @McpTool(description = "Look up a tenant in a multitenant CAS deployment: its description, its authentication, "
        + "user interface and delegated authentication policies, and the names of the settings it overrides. "
        + "Setting values are never returned. When the tenant cannot be found, the ids of the known tenants are listed.",
        title = "Find Tenant",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_ADMIN')")
    public TenantLookup findTenant(
        @McpToolParam(description = "The id of the tenant")
        final String tenantId) {
        val manager = tenantsManager.getIfAvailable();
        if (manager == null) {
            return new TenantLookup(tenantId, null, null, "Multitenancy is not configured");
        }
        return manager.findTenant(tenantId)
            .map(tenant -> new TenantLookup(tenantId, summarize(tenant), null, null))
            .orElseGet(() -> new TenantLookup(tenantId, null,
                manager.findTenants().stream().map(TenantDefinition::getId).sorted().toList(), "No tenant matches " + tenantId));
    }

    private static TenantSummary summarize(final TenantDefinition tenant) {
        return new TenantSummary(tenant.getId(), tenant.getDescription(),
            new TreeSet<>(tenant.getProperties().keySet()).stream().toList(),
            tenant.getAuthenticationPolicy(), tenant.getUserInterfacePolicy(), tenant.getDelegatedAuthenticationPolicy());
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TenantLookup(String tenantId, @Nullable TenantSummary tenant, @Nullable List<String> knownTenants,
                               @Nullable String reason) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TenantSummary(String id, @Nullable String description, List<String> overriddenSettings,
                                @Nullable Object authenticationPolicy, @Nullable Object userInterfacePolicy,
                                @Nullable Object delegatedAuthenticationPolicy) {
    }
}

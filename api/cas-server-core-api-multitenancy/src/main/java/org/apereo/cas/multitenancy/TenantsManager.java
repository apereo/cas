package org.apereo.cas.multitenancy;

import module java.base;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link TenantsManager}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public interface TenantsManager {
    /**
     * Tenant manager bean name.
     */
    String BEAN_NAME = "tenantsManager";

    /**
     * Find tenant.
     *
     * @param tenantId the tenant id
     * @return the tenant definition
     */
    Optional<TenantDefinition> findTenant(@Nullable String tenantId);

    /**
     * Find tenants list.
     *
     * @return the list
     */
    List<TenantDefinition> findTenants();

    /**
     * Save/register a tenant definition.
     *
     * @param tenantDefinition the tenant definition to register
     * @return the saved tenant definition
     */
    TenantDefinition save(TenantDefinition tenantDefinition);

    /**
     * Delete a tenant definition by its id.
     *
     * @param tenantId the tenant id to delete
     * @return true if a tenant was removed, false if no tenant with that id was found
     */
    boolean delete(String tenantId);

    /**
     * Load.
     */
    void load();
}

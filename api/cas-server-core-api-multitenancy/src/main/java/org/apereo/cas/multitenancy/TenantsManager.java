package org.apereo.cas.multitenancy;

import java.util.List;
import java.util.Optional;

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
    Optional<TenantDefinition> findTenant(String tenantId);

    /**
     * Find tenants list.
     *
     * @return the list
     */
    List<TenantDefinition> findTenants();
}

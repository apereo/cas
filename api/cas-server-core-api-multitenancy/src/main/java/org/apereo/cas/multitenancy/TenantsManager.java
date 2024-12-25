package org.apereo.cas.multitenancy;

import java.util.Optional;

/**
 * This is {@link TenantsManager}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@FunctionalInterface
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
}

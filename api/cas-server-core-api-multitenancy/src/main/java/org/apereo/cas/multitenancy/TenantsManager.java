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
     * Value shown in place of a property value that is not disclosed, such as a password or a key.
     * Saving a definition with this value keeps the value already stored for that property.
     */
    String REDACTED_PROPERTY_VALUE = "******";

    /**
     * The form of a tenant id: letters, digits, dots, dashes and underscores, starting with a letter or a digit.
     * Tenant ids become part of urls and cookie paths, so other characters are not accepted.
     */
    Pattern TENANT_ID_PATTERN = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9_.-]*$");

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

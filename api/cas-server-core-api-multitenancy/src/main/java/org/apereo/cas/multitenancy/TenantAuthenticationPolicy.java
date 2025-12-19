package org.apereo.cas.multitenancy;

import module java.base;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link TenantAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface TenantAuthenticationPolicy extends Serializable {
    /**
     * Gets authentication handlers.
     *
     * @return the authentication handlers
     */
    @Nullable List<String> getAuthenticationHandlers();

    /**
     * Gets attribute repositories.
     *
     * @return the attribute repositories
     */
    @Nullable List<String> getAttributeRepositories();

    /**
     * Gets authentication protocol policy.
     *
     * @return the authentication protocol policy
     */
    @Nullable TenantAuthenticationProtocolPolicy getAuthenticationProtocolPolicy();
}

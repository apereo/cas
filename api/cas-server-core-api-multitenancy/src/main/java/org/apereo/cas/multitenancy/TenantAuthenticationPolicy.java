package org.apereo.cas.multitenancy;

import java.io.Serializable;
import java.util.List;

/**
 * This is {@link TenantAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public interface TenantAuthenticationPolicy extends Serializable {
    /**
     * Gets authentication handlers.
     *
     * @return the authentication handlers
     */
    List<String> getAuthenticationHandlers();


    /**
     * Gets allowed external identity providers for delegation.
     *
     * @return the allowed providers
     */
    List<String> getAllowedProviders();
}

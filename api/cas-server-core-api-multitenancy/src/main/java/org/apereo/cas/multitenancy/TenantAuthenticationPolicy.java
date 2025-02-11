package org.apereo.cas.multitenancy;

import java.io.Serializable;
import java.util.List;

/**
 * This is {@link TenantAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@FunctionalInterface
public interface TenantAuthenticationPolicy extends Serializable {
    /**
     * Gets authentication handlers.
     *
     * @return the authentication handlers
     */
    List<String> getAuthenticationHandlers();
}

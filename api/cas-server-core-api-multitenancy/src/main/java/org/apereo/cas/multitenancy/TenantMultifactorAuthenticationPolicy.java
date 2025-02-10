package org.apereo.cas.multitenancy;

import java.io.Serializable;
import java.util.Set;

/**
 * This is {@link TenantMultifactorAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@FunctionalInterface
public interface TenantMultifactorAuthenticationPolicy extends Serializable {

    /**
     * Gets global provider ids.
     *
     * @return the global provider ids
     */
    Set<String> getGlobalProviderIds();
}

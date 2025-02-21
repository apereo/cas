package org.apereo.cas.multitenancy;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.Set;

/**
 * This is {@link TenantMultifactorAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface TenantMultifactorAuthenticationPolicy extends Serializable {

    /**
     * Gets global provider ids.
     *
     * @return the global provider ids
     */
    Set<String> getGlobalProviderIds();
}

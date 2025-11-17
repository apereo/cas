package org.apereo.cas.multitenancy;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.jspecify.annotations.Nullable;
import java.io.Serializable;
import java.util.List;

/**
 * This is {@link TenantDelegatedAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface TenantDelegatedAuthenticationPolicy extends Serializable {
    /**
     * Gets allowed external identity providers for delegation.
     *
     * @return the allowed providers
     */
    @Nullable List<String> getAllowedProviders();
}

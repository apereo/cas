package org.apereo.cas.pac4j.discovery;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.pac4j.core.client.IndirectClient;

import java.util.Optional;

/**
 * This is {@link DelegatedAuthenticationDynamicDiscoveryProviderLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@FunctionalInterface
public interface DelegatedAuthenticationDynamicDiscoveryProviderLocator {

    /**
     * Locate.
     *
     * @param request the request
     * @return the client
     */
    Optional<IndirectClient> locate(DynamicDiscoveryProviderRequest request);

    @SuperBuilder
    @Getter
    class DynamicDiscoveryProviderRequest {
        private final String userId;
    }
}

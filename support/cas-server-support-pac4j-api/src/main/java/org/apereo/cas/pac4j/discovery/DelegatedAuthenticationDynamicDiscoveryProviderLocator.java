package org.apereo.cas.pac4j.discovery;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.WebContext;

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
     * Attribute name in the request scope to indicate the direct url.
     */
    String REQUEST_SCOPE_ATTR_PROVIDER_REDIRECT_URL = "delegatedAuthProviderRedirectUrl";


    /**
     * Locate.
     *
     * @param request    the request
     * @param webContext the web context
     * @return the client
     * @throws Throwable the throwable
     */
    Optional<IndirectClient> locate(DynamicDiscoveryProviderRequest request, WebContext webContext) throws Throwable;

    @SuperBuilder
    @Getter
    class DynamicDiscoveryProviderRequest {
        private final String userId;
    }
}

package org.apereo.cas.pac4j.client;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;

import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link DelegatedClientIdentityProviderRedirectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@FunctionalInterface
public interface DelegatedClientIdentityProviderRedirectionStrategy {
    /**
     * Determine auto redirect policy for provider.
     *
     * @param context  the context
     * @param service  the service
     * @param provider the provider
     */
    Optional<DelegatedClientIdentityProviderConfiguration> getPrimaryDelegatedAuthenticationProvider(RequestContext context,
                                                                                                     WebApplicationService service,
                                                                                                     DelegatedClientIdentityProviderConfiguration provider);
}

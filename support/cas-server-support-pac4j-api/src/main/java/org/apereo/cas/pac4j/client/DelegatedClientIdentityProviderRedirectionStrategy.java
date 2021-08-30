package org.apereo.cas.pac4j.client;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;

import org.springframework.core.Ordered;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link DelegatedClientIdentityProviderRedirectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@FunctionalInterface
public interface DelegatedClientIdentityProviderRedirectionStrategy extends Ordered {
    /**
     * Determine auto redirect policy for provider.
     *
     * @param context  the context
     * @param service  the service
     * @param provider the provider
     * @return the primary delegated authentication provider
     */
    Optional<DelegatedClientIdentityProviderConfiguration> getPrimaryDelegatedAuthenticationProvider(RequestContext context,
                                                                                                     WebApplicationService service,
                                                                                                     DelegatedClientIdentityProviderConfiguration provider);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}

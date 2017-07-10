package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;

import java.util.Collection;

/**
 * This is {@link MultifactorAuthenticationProviderSelector}
 * that decides how to resolve a single provider from a collection available
 * to a registered service.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@FunctionalInterface
public interface MultifactorAuthenticationProviderSelector {

    /**
     * Resolve multifactor authentication provider.
     *
     * @param providers the providers
     * @param service   the service
     * @param principal the principal
     * @return the multifactor authentication provider
     */
    MultifactorAuthenticationProvider resolve(Collection<MultifactorAuthenticationProvider> providers,
                                              RegisteredService service,
                                              Principal principal);
}

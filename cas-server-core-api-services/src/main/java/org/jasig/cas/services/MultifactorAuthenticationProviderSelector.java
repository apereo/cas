package org.jasig.cas.services;

import org.jasig.cas.authentication.principal.Principal;

import java.util.Collection;
import java.util.Set;

/**
 * This is {@link MultifactorAuthenticationProviderSelector}
 * that decides how to resolve a single provider from a collection available
 * to a registered service.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
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

package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;
import org.jspecify.annotations.Nullable;

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
     * Default implementation bean name.
     */
    String BEAN_NAME = "multifactorAuthenticationProviderSelector";

    /**
     * Resolve multifactor authentication provider.
     *
     * @param providers the providers
     * @param service   the service
     * @param principal the principal
     * @return the multifactor authentication provider
     * @throws Throwable the throwable
     */
    @Nullable MultifactorAuthenticationProvider resolve(Collection<MultifactorAuthenticationProvider> providers,
                                                        @Nullable RegisteredService service, Principal principal) throws Throwable;
}

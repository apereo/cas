package org.apereo.cas.authentication;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link MultifactorAuthenticationProviderResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface MultifactorAuthenticationProviderResolver {

    /**
     * Locate the provider in the collection, and have it match the requested mfa.
     * If the provider is multi-instance, resolve based on inner-registered providers.
     *
     * @param providers        the providers
     * @param requestMfaMethod the request mfa method
     * @return the optional
     */
    Optional<MultifactorAuthenticationProvider> resolveProvider(Map<String, MultifactorAuthenticationProvider> providers,
                                                                Collection<String> requestMfaMethod);
}

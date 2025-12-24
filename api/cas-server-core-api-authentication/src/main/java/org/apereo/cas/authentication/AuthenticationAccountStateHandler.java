package org.apereo.cas.authentication;

import module java.base;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link AuthenticationAccountStateHandler}.
 *
 * @author Misagh Moayyed
 * @param <AuthnResponse> the type parameter
 * @param <Configuration> the type parameter
 * @since 5.3.0
 */
@FunctionalInterface
public interface AuthenticationAccountStateHandler<AuthnResponse, Configuration> {
    /**
     * Handle account state.
     *
     * @param response      the response
     * @param configuration the configuration
     * @return the list
     * @throws Throwable the throwable
     */
    @Nullable List<MessageDescriptor> handle(@Nullable AuthnResponse response, Configuration configuration) throws Throwable;
}


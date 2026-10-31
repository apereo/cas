package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationConverter;

/**
 * This is {@link DefaultOAuth20ClientAuthenticationProvider}, a client authentication method made of a converter and
 * an {@link OAuth20ClientCredentialsAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Getter
public class DefaultOAuth20ClientAuthenticationProvider implements OAuth20ClientAuthenticationProvider {
    private final String name;

    private final int order;

    private final AuthenticationConverter authenticationConverter;

    private final OAuth20ClientCredentialsAuthenticator authenticator;

    private final AuthenticationProvider authenticationProvider;

    private final boolean requestedClientIgnored;

    @Builder
    public DefaultOAuth20ClientAuthenticationProvider(final String name, final int order,
                                                      final AuthenticationConverter authenticationConverter,
                                                      final OAuth20ClientCredentialsAuthenticator authenticator,
                                                      final boolean requestedClientIgnored) {
        this.name = name;
        this.order = order;
        this.authenticationConverter = authenticationConverter;
        this.authenticator = authenticator;
        this.requestedClientIgnored = requestedClientIgnored;
        this.authenticationProvider = new ClientCredentialsAuthenticationProvider(authenticator);
    }

    @RequiredArgsConstructor
    private static final class ClientCredentialsAuthenticationProvider implements AuthenticationProvider {
        private final OAuth20ClientCredentialsAuthenticator authenticator;

        @Override
        public @Nullable Authentication authenticate(final Authentication authentication) throws AuthenticationException {
            val token = (OAuth20ClientAuthenticationToken) authentication;
            return authenticator.authenticate(token).map(token::authenticated).orElse(null);
        }

        @Override
        public boolean supports(final Class<?> authentication) {
            return OAuth20ClientAuthenticationToken.class.isAssignableFrom(authentication);
        }
    }
}

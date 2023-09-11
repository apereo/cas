package org.apereo.cas.api;

import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.impl.token.PasswordlessAuthenticationToken;

import org.springframework.core.Ordered;

/**
 * This is {@link PasswordlessAuthenticationPreProcessor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface PasswordlessAuthenticationPreProcessor extends Ordered {
    /**
     * Process.
     *
     * @param authenticationResultBuilder the authentication result builder
     * @param principal                   the principal
     * @param service                     the service
     * @param credential                  the credential
     * @param token                       the token
     * @return the authentication result builder
     * @throws Throwable the throwable
     */
    AuthenticationResultBuilder process(AuthenticationResultBuilder authenticationResultBuilder,
                                        PasswordlessUserAccount principal,
                                        Service service,
                                        Credential credential,
                                        PasswordlessAuthenticationToken token) throws Throwable;

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}

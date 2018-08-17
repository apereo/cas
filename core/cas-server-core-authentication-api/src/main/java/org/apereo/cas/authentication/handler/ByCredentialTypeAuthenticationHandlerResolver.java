package org.apereo.cas.authentication.handler;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerResolver;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.util.CollectionUtils;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Set;

/**
 * This is {@link ByCredentialTypeAuthenticationHandlerResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
public class ByCredentialTypeAuthenticationHandlerResolver implements AuthenticationHandlerResolver {
    private final Collection<Class<? extends Credential>> credentials;

    public ByCredentialTypeAuthenticationHandlerResolver(final Class<? extends Credential>... credentials) {
        this(CollectionUtils.wrapSet(credentials));
    }

    @Override
    public boolean supports(final Set<AuthenticationHandler> handlers, final AuthenticationTransaction transaction) {
        return credentials.stream().anyMatch(transaction::hasCredentialOfType);
    }
}

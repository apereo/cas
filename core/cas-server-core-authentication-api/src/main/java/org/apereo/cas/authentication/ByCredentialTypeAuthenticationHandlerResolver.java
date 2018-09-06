package org.apereo.cas.authentication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.CollectionUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link ByCredentialTypeAuthenticationHandlerResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class ByCredentialTypeAuthenticationHandlerResolver implements AuthenticationHandlerResolver {
    private final Collection<Class<? extends Credential>> credentials;

    public ByCredentialTypeAuthenticationHandlerResolver(final Class<? extends Credential>... credentials) {
        this(CollectionUtils.wrapSet(credentials));
    }

    @Override
    public Set<AuthenticationHandler> resolve(final Set<AuthenticationHandler> candidateHandlers, final AuthenticationTransaction transaction) {
        final Set<Credential> supportedCreds = supported(transaction.getCredentials());
        return candidateHandlers.stream().filter(h -> supportedCreds.stream().anyMatch(c -> h.supports(c)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public boolean supports(final Set<AuthenticationHandler> handlers, final AuthenticationTransaction transaction) {
        return credentials.stream().anyMatch(transaction::hasCredentialOfType);
    }

    private Set<Credential> supported(final Collection<? extends Credential> candidateCredentials) {
        return candidateCredentials.stream().filter(c -> credentials.contains(c.getClass()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}

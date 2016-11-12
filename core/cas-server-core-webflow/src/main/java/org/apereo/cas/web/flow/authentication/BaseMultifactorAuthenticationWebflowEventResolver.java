package org.apereo.cas.web.flow.authentication;

import com.google.common.collect.Sets;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.VariegatedMultifactorAuthenticationProvider;
import org.apereo.cas.web.flow.authn.MultifactorAuthenticationWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.AbstractCasWebflowEventResolver;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link BaseMultifactorAuthenticationWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseMultifactorAuthenticationWebflowEventResolver extends AbstractCasWebflowEventResolver
        implements MultifactorAuthenticationWebflowEventResolver {

    @Override
    public Optional<MultifactorAuthenticationProvider> resolveProvider(final Map<String, MultifactorAuthenticationProvider> providers,
                                                                       final Collection<String> requestMfaMethod) {
        final Optional<MultifactorAuthenticationProvider> providerFound = providers.values().stream()
                .filter(p -> requestMfaMethod.stream().filter(m -> p.matches(m)).findFirst().isPresent())
                .findFirst();
        if (providerFound.isPresent()) {
            final MultifactorAuthenticationProvider provider = providerFound.get();
            if (provider instanceof VariegatedMultifactorAuthenticationProvider) {
                final VariegatedMultifactorAuthenticationProvider multi = VariegatedMultifactorAuthenticationProvider.class.cast(provider);
                final Optional<MultifactorAuthenticationProvider> instance = multi.getProviders().stream()
                        .filter(p -> requestMfaMethod.stream().filter(m -> p.matches(m)).findFirst().isPresent())
                        .findFirst();
                return instance;
            }
        }

        return providerFound;
    }

    @Override
    public Optional<MultifactorAuthenticationProvider> resolveProvider(final Map<String, MultifactorAuthenticationProvider> providers,
                                                                       final String... requestMfaMethod) {
        return resolveProvider(providers, Sets.newHashSet(requestMfaMethod));
    }

    @Override
    public Optional<MultifactorAuthenticationProvider> resolveProvider(final Map<String, MultifactorAuthenticationProvider> providers,
                                                                       final String requestMfaMethod) {
        return resolveProvider(providers, Sets.newHashSet(requestMfaMethod));
    }

    @Override
    public Collection<MultifactorAuthenticationProvider> flattenProviders(final Collection<MultifactorAuthenticationProvider> providers) {
        final Set providersSet = providers.stream()
                .map(p -> {
                    if (p instanceof VariegatedMultifactorAuthenticationProvider) {
                        return Stream.of(VariegatedMultifactorAuthenticationProvider.class.cast(p).getProviders());
                    }
                    return p;
                })
                .collect(Collectors.toSet());
        return providersSet;
    }
}

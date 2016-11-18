package org.apereo.cas.web.flow.authentication;

import com.google.common.collect.Sets;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderResolver;
import org.apereo.cas.services.VariegatedMultifactorAuthenticationProvider;
import org.apereo.cas.web.flow.resolver.impl.AbstractCasWebflowEventResolver;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link BaseMultifactorAuthenticationProviderEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseMultifactorAuthenticationProviderEventResolver extends AbstractCasWebflowEventResolver
        implements MultifactorAuthenticationProviderResolver {

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


    /**
     * Locate the provider in the collection, and have it match the requested mfa.
     * If the provider is multi-instance, resolve based on inner-registered providers.
     *
     * @param providers        the providers
     * @param requestMfaMethod the request mfa method
     * @return the optional
     */
    public Optional<MultifactorAuthenticationProvider> resolveProvider(final Map<String, MultifactorAuthenticationProvider> providers,
                                                                       final String... requestMfaMethod) {
        return resolveProvider(providers, Sets.newHashSet(requestMfaMethod));
    }

    /**
     * Locate the provider in the collection, and have it match the requested mfa.
     * If the provider is multi-instance, resolve based on inner-registered providers.
     *
     * @param providers        the providers
     * @param requestMfaMethod the request mfa method
     * @return the optional
     */
    public Optional<MultifactorAuthenticationProvider> resolveProvider(final Map<String, MultifactorAuthenticationProvider> providers,
                                                                       final String requestMfaMethod) {
        return resolveProvider(providers, Sets.newHashSet(requestMfaMethod));
    }

    @Override
    public Collection<MultifactorAuthenticationProvider> flattenProviders(final Collection<? extends MultifactorAuthenticationProvider> providers) {
        final Collection<MultifactorAuthenticationProvider> flattenedProviders = Sets.newHashSet();
        providers.stream().forEach(p -> {
            if (p instanceof VariegatedMultifactorAuthenticationProvider) {
                flattenedProviders.addAll(VariegatedMultifactorAuthenticationProvider.class.cast(p).getProviders());
            } else {
                flattenedProviders.add(p);
            }
        });

        return flattenedProviders;
    }
}

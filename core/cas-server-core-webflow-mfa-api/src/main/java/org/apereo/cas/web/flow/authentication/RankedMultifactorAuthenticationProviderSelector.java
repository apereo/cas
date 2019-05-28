package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link RankedMultifactorAuthenticationProviderSelector}
 * that sorts providers based on their rank and picks the one with
 * the highest priority.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class RankedMultifactorAuthenticationProviderSelector implements MultifactorAuthenticationProviderSelector {

    @Override
    public MultifactorAuthenticationProvider resolve(final Collection<MultifactorAuthenticationProvider> providers,
                                                     final RegisteredService service,
                                                     final Principal principal) {
        val sorted = new ArrayList<MultifactorAuthenticationProvider>(providers);
        if (sorted.isEmpty()) {
            throw new IllegalArgumentException("List of candidate multifactor authentication providers is empty");
        }
        AnnotationAwareOrderComparator.sort(sorted);
        return selectMultifactorAuthenticationProvider(service, sorted);
    }

    /**
     * Select multifactor authentication provider.
     *
     * @param service   the service
     * @param providers the providers
     * @return the multifactor authentication provider
     */
    protected MultifactorAuthenticationProvider selectMultifactorAuthenticationProvider(
        final RegisteredService service,
        final List<MultifactorAuthenticationProvider> providers) {
        val provider = providers.get(providers.size() - 1);
        LOGGER.debug("Selected the provider [{}] for service [{}] out of [{}] providers", provider, service, providers.size());
        return provider;
    }
}

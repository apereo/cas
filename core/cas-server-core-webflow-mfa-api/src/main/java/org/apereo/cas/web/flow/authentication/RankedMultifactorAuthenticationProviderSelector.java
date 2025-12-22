package org.apereo.cas.web.flow.authentication;

import module java.base;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

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
                                                     @Nullable final RegisteredService service,
                                                     final Principal principal) {
        val sorted = new ArrayList<>(providers);
        if (sorted.isEmpty()) {
            throw new IllegalArgumentException("List of candidate multifactor authentication providers is empty");
        }
        AnnotationAwareOrderComparator.sort(sorted);
        return selectMultifactorAuthenticationProvider(service, sorted);
    }

    protected MultifactorAuthenticationProvider selectMultifactorAuthenticationProvider(
        @Nullable final RegisteredService service,
        final List<MultifactorAuthenticationProvider> providers) {
        val provider = providers.getLast();
        LOGGER.debug("Selected the provider [{}] for service [{}] out of [{}] providers", provider, service, providers.size());
        return provider;
    }
}

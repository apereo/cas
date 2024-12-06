package org.apereo.cas.pac4j.client;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;

import org.jooq.lambda.Unchecked;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.execution.RequestContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link ChainingDelegatedClientIdentityProviderRedirectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class ChainingDelegatedClientIdentityProviderRedirectionStrategy implements DelegatedClientIdentityProviderRedirectionStrategy {
    private final List<DelegatedClientIdentityProviderRedirectionStrategy> strategies = new ArrayList<>();

    /**
     * Add strategy.
     *
     * @param strategy the strategy
     */
    public void addStrategy(final DelegatedClientIdentityProviderRedirectionStrategy strategy) {
        this.strategies.add(strategy);
        AnnotationAwareOrderComparator.sort(this.strategies);
    }

    @Override
    public Optional<DelegatedClientIdentityProviderConfiguration> select(
        final RequestContext context,
        final WebApplicationService service,
        final Set<DelegatedClientIdentityProviderConfiguration> providers) {
        return strategies
            .stream()
            .map(Unchecked.function(strategy -> strategy.select(context, service, providers)))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }
}

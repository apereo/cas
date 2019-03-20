package org.apereo.cas.web.flow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.RequestContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link ChainingSingleSignOnParticipationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class ChainingSingleSignOnParticipationStrategy implements SingleSignOnParticipationStrategy {
    private final List<SingleSignOnParticipationStrategy> providers = new ArrayList<>();

    /**
     * Add provider.
     *
     * @param provider the provider
     */
    public void addStrategy(final SingleSignOnParticipationStrategy provider) {
        providers.add(provider);
    }

    /**
     * Add providers.
     *
     * @param provider the provider
     */
    public void addStrategy(final List<SingleSignOnParticipationStrategy> provider) {
        providers.addAll(provider);
    }

    @Override
    public boolean isParticipating(final RequestContext context) {
        val supporters = providers.stream()
            .filter(p -> p.supports(context))
            .collect(Collectors.toList());

        if (supporters.isEmpty()) {
            return SingleSignOnParticipationStrategy.alwaysParticipating().isParticipating(context);
        }
        return supporters.stream().allMatch(p -> p.isParticipating(context));
    }
}

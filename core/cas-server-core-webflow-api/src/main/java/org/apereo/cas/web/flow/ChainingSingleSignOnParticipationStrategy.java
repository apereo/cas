package org.apereo.cas.web.flow;

import org.apereo.cas.util.model.TriStateBoolean;

import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link ChainingSingleSignOnParticipationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
public class ChainingSingleSignOnParticipationStrategy implements SingleSignOnParticipationStrategy {
    private final List<SingleSignOnParticipationStrategy> providers = new ArrayList<>(0);

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
    public boolean isParticipating(final SingleSignOnParticipationRequest ssoRequest) {
        val supporters = getSupportingSingleSignOnParticipationStrategies(ssoRequest);
        if (supporters.isEmpty()) {
            return SingleSignOnParticipationStrategy.alwaysParticipating().isParticipating(ssoRequest);
        }
        return supporters.stream().allMatch(p -> p.isParticipating(ssoRequest));
    }

    @Override
    public boolean supports(final SingleSignOnParticipationRequest ssoRequest) {
        return providers.stream().anyMatch(p -> p.supports(ssoRequest));
    }

    @Override
    public TriStateBoolean isCreateCookieOnRenewedAuthentication(final SingleSignOnParticipationRequest context) {
        val supporters = getSupportingSingleSignOnParticipationStrategies(context);
        val result = supporters.stream().allMatch(p -> {
            val createCookieOnRenewedAuthentication = p.isCreateCookieOnRenewedAuthentication(context);
            return createCookieOnRenewedAuthentication.isTrue() || createCookieOnRenewedAuthentication.isUndefined();
        });
        return TriStateBoolean.fromBoolean(result);
    }

    private List<SingleSignOnParticipationStrategy> getSupportingSingleSignOnParticipationStrategies(
        final SingleSignOnParticipationRequest context) {
        return providers.stream()
            .filter(p -> p.supports(context))
            .collect(Collectors.toList());
    }

}

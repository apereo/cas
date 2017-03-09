package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is {@link DefaultAuthenticationServiceSelectionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultAuthenticationServiceSelectionPlan implements AuthenticationServiceSelectionPlan {
    private final List<AuthenticationServiceSelectionStrategy> strategies; 

    public DefaultAuthenticationServiceSelectionPlan() {
        this.strategies = new ArrayList<>();
    }

    public DefaultAuthenticationServiceSelectionPlan(final AuthenticationServiceSelectionStrategy... strategies) {
        this.strategies = Arrays.asList(strategies);
    }

    @Override
    public void registerStrategy(final AuthenticationServiceSelectionStrategy strategy) {
        strategies.add(strategy);
    }

    @Override
    public Service resolveService(final Service service) {
        return this.strategies.stream()
                .filter(s -> s.supports(service))
                .findFirst()
                .get()
                .resolveServiceFrom(service);
    }
}

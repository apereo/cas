package org.apereo.cas.authentication;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Service;
import org.springframework.core.OrderComparator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultAuthenticationServiceSelectionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@AllArgsConstructor
public class DefaultAuthenticationServiceSelectionPlan implements AuthenticationServiceSelectionPlan {
    private final List<AuthenticationServiceSelectionStrategy> strategies;

    public DefaultAuthenticationServiceSelectionPlan(final AuthenticationServiceSelectionStrategy... strategies) {
        this.strategies = Arrays.stream(strategies).collect(Collectors.toList());
        OrderComparator.sort(this.strategies);
    }

    @Override
    public void registerStrategy(final AuthenticationServiceSelectionStrategy strategy) {
        strategies.add(strategy);
        OrderComparator.sort(this.strategies);
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

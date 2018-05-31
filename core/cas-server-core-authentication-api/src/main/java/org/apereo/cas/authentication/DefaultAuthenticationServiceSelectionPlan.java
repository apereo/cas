package org.apereo.cas.authentication;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
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
        final var strategy = this.strategies
            .stream()
            .filter(s -> s.supports(service))
            .findFirst();

        if (strategy.isPresent()) {
            final var result = strategy.get();
            return result.resolveServiceFrom(service);
        }
        return null;
    }

    @Override
    public <T extends Service> T resolveService(final Service service, final Class<T> clazz) {
        final var result = resolveService(service);
        if (result == null) {
            return null;
        }
        if (!clazz.isAssignableFrom(result.getClass())) {
            throw new ClassCastException("Object [" + result + " is of type " + result.getClass() + " when we were expecting " + clazz);
        }
        return (T) result;
    }
}

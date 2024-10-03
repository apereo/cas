package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.spring.beans.BeanSupplier;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultAuthenticationServiceSelectionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class DefaultAuthenticationServiceSelectionPlan implements AuthenticationServiceSelectionPlan {
    private final List<AuthenticationServiceSelectionStrategy> strategies;

    public DefaultAuthenticationServiceSelectionPlan(final AuthenticationServiceSelectionStrategy... strategies) {
        this.strategies = Arrays.stream(strategies).collect(Collectors.toList());
        AnnotationAwareOrderComparator.sort(this.strategies);
    }

    @Override
    public void registerStrategy(final AuthenticationServiceSelectionStrategy strategy) {
        if (BeanSupplier.isNotProxy(strategy)) {
            strategies.add(strategy);
            AnnotationAwareOrderComparator.sort(this.strategies);
        }
    }

    @Override
    public Service resolveService(final Service service) throws Throwable {
        val strategy = this.strategies
            .stream()
            .filter(selectionStrategy -> selectionStrategy.supports(service))
            .findFirst();

        if (strategy.isPresent()) {
            val result = strategy.get();
            return result.resolveServiceFrom(service);
        }
        return null;
    }

    @Override
    public <T extends Service> T resolveService(final Service service, final Class<T> clazz) throws Throwable {
        val result = resolveService(service);
        if (result == null) {
            return null;
        }
        if (!clazz.isAssignableFrom(result.getClass())) {
            throw new ClassCastException("Object [" + result + " is of type " + result.getClass() + " when we were expecting " + clazz);
        }
        return (T) result;
    }
}

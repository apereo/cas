package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.core.Ordered;

import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * This is {@link DefaultServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class DefaultServicesManagerRegisteredServiceLocator implements ServicesManagerRegisteredServiceLocator {
    private int order = Ordered.LOWEST_PRECEDENCE;

    private BiPredicate<RegisteredService, Service> registeredServiceFilter =
        (registeredService, service) -> registeredService.getClass().equals(RegexRegisteredService.class);

    @Override
    public RegisteredService locate(final Collection<RegisteredService> candidates, final Service service,
        final Predicate<RegisteredService> requestedFilter) {
        return candidates
            .stream()
            .filter(entry -> registeredServiceFilter.test(entry, service))
            .filter(requestedFilter)
            .findFirst()
            .orElse(null);
    }
}

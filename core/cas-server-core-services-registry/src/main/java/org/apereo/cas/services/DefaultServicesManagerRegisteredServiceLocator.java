package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.springframework.core.Ordered;

import java.util.Collection;
import java.util.function.BiPredicate;

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

    private BiPredicate<RegisteredService, Service> registeredServiceFilter = (registeredService, service) -> {
        val supportedType = supports(registeredService, service);
        return supportedType && registeredService.matches(service.getId());
    };

    @Override
    public RegisteredService locate(final Collection<RegisteredService> candidates, final Service service) {
        return candidates
            .stream()
            .filter(registeredService -> supports(registeredService, service))
            .filter(registeredService -> registeredServiceFilter.test(registeredService, service))
            .findFirst()
            .orElse(null);
    }

    @Override
    public boolean supports(final RegisteredService registeredService, final Service service) {
        return RegexRegisteredService.class.isAssignableFrom(registeredService.getClass())
            && registeredService.getFriendlyName().equalsIgnoreCase(RegexRegisteredService.FRIENDLY_NAME);
    }
}

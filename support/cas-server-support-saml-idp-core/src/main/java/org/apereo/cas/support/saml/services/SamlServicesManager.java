package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.AbstractServicesManager;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.authentication.principal.SamlService;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Implementation of the {@link ServicesManager} interface that supports SAML registered services.
 *
 * @author Travis Schmidt
 * @author Dmitriy Kopylenko
 * @since 6.2.0
 */
public class SamlServicesManager extends AbstractServicesManager {

    public SamlServicesManager(final ServiceRegistry serviceRegistry, final ApplicationEventPublisher eventPublisher, final Set<String> environments) {
        super(serviceRegistry, eventPublisher, environments);
    }

    protected Stream<RegisteredService> getCandidateServicesToMatch(final String serviceId) {
        return getServices().values().stream().sorted(Comparator.naturalOrder());
    }

    @Override
    public boolean supports(final Service service) {
        return service instanceof SamlService;
    }

    @Override
    public boolean supports(final RegisteredService service) {
        return service instanceof SamlRegisteredService;
    }

    @Override
    public boolean supports(final Class clazz) {
        return clazz.isAssignableFrom(SamlRegisteredService.class);
    }
}

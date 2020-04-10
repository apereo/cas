package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.AbstractServicesManager;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.authentication.principal.SamlService;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import static java.util.stream.Collectors.toSet;

/**
 * Implementation of the {@link ServicesManager} interface that supports SAML registered services.
 *
 * @author Travis Schmidt
 * @author Dmitriy Kopylenko
 * @since 6.2.0
 */
public class SamlServicesManager extends AbstractServicesManager {

    private Set<RegisteredService> orderedServices = new ConcurrentSkipListSet<>();

    public SamlServicesManager(final ServiceRegistry serviceRegistry, final ApplicationEventPublisher eventPublisher, final Set<String> environments) {
        super(serviceRegistry, eventPublisher, environments);
    }

    @Override
    protected Collection<RegisteredService> getCandidateServicesToMatch(final String serviceId) {
        return this.orderedServices.stream()
                .filter(r -> r.matches(serviceId))
                .filter(this::supports)
                .collect(toSet());
    }

    @Override
    protected void deleteInternal(final RegisteredService service) {
        this.orderedServices.remove(service);
    }

    @Override
    protected void saveInternal(final RegisteredService service) {
        this.orderedServices.clear();
        this.orderedServices.addAll(getAllServices());
    }

    @Override
    protected void loadInternal() {
        this.orderedServices.clear();
        this.orderedServices.addAll(getAllServices());
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

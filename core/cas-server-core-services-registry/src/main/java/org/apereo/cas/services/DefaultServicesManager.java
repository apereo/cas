package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Default implementation of the {@link ServicesManager} interface.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class DefaultServicesManager extends AbstractServicesManager {

    private final Set<RegisteredService> orderedServices = new ConcurrentSkipListSet<>();

    public DefaultServicesManager(final ServiceRegistry serviceRegistry, final ApplicationEventPublisher eventPublisher, final Set<String> environments) {
        super(serviceRegistry, eventPublisher, environments);
    }

    @Override
    protected Collection<RegisteredService> getCandidateServicesToMatch(final String serviceId) {
        return this.orderedServices;
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
    protected boolean filterInternal(final RegisteredService registeredService) {
        return supports(registeredService);
    }

    @Override
    public boolean supports(final Service service) {
        return service != null
                && !service.getClass().getCanonicalName().equals("org.apereo.cas.support.oauth.authentication.principal.OAuthWebApplicationService")
                && !service.getClass().getCanonicalName().equals("org.apereo.cas.support.saml.authentication.principal.Saml20WebApplicationService");
    }

    @Override
    public boolean supports(final RegisteredService service) {
        return service != null && service.getClass().getCanonicalName().equals(RegexRegisteredService.class.getCanonicalName());
    }
}

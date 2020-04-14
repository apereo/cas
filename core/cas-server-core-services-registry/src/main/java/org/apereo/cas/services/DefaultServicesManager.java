package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;

import org.springframework.context.ApplicationEventPublisher;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;


/**
 * Default implementation of the {@link ServicesManager} interface.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class DefaultServicesManager extends AbstractServicesManager {

    public DefaultServicesManager(final ServiceRegistry serviceRegistry,
                                  final ApplicationEventPublisher eventPublisher,
                                  final Set<String> environments) {
        super(serviceRegistry, eventPublisher, environments);
    }

    protected Stream<RegisteredService> getCandidateServicesToMatch(final String serviceId) {
        return getServices().values().stream().sorted(Comparator.naturalOrder());
    }

    @Override
    public boolean supports(final Service service) {
        return service != null
                && !service.getClass().getCanonicalName().equals("org.apereo.cas.support.saml.authentication.principal.SamlService");

    }

    @Override
    public boolean supports(final RegisteredService service) {
        return service != null
                && service.getClass().getCanonicalName().equals(RegexRegisteredService.class.getCanonicalName());
    }
}

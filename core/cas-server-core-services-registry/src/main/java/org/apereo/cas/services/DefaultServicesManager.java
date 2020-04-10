package org.apereo.cas.services;

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

    @Override
    protected Stream<RegisteredService> getCandidateServicesToMatch(final String serviceId) {
        return getServices().values().stream().sorted(Comparator.naturalOrder());
    }

}

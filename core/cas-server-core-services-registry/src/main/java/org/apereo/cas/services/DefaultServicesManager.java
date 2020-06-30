package org.apereo.cas.services;

import com.github.benmanes.caffeine.cache.Cache;
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
            final Set<String> environments,
            final Cache<Long, RegisteredService> services) {
        super(serviceRegistry, eventPublisher, environments, services);
    }

    @Override
    protected Stream<RegisteredService> getCandidateServicesToMatch(final String serviceId) {
        return getServices().asMap().values().stream().sorted(Comparator.naturalOrder());
    }

}

package org.apereo.cas.services;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Default implementation of the {@link ServicesManager} interface.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class DefaultServicesManager extends AbstractServicesManager {

    public DefaultServicesManager(final ServicesManagerConfigurationContext context) {
        super(context);
    }

    @Override
    protected Collection<RegisteredService> getCandidateServicesToMatch(final String serviceId) {
        return getConfigurationContext().getServicesCache()
            .asMap()
            .values()
            .stream()
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.toList());
    }

    @Override
    public Collection<RegisteredService> getServicesForDomain(final String domain) {
        return getConfigurationContext().getServicesCache()
            .asMap()
            .values()
            .stream()
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.toList());
    }
}

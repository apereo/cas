package org.apereo.cas.services.mgmt;

import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerConfigurationContext;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of the {@link ServicesManager} interface.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Monitorable
public class DefaultServicesManager extends AbstractServicesManager {

    public DefaultServicesManager(final ServicesManagerConfigurationContext context) {
        super(context);
    }

    @Override
    public Collection<RegisteredService> getServicesForDomain(final String domain) {
        return collectServices();
    }

    @Override
    protected Collection<RegisteredService> getCandidateServicesToMatch(final String serviceId) {
        return collectServices();
    }

    private List<RegisteredService> collectServices() {
        return getCacheableServicesStream()
            .get()
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.toList());
    }
}

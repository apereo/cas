package org.apereo.cas.services.mgmt;

import module java.base;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import lombok.val;
import org.jspecify.annotations.Nullable;

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
    public @Nullable Collection<RegisteredService> getServicesForDomain(final String domain) {
        return collectServices();
    }

    @Override
    protected @Nullable Collection<RegisteredService> getCandidateServicesToMatch(final String serviceId) {
        return collectServices();
    }

    private @Nullable List<RegisteredService> collectServices() {
        val cacheEnabled = configurationContext.getCasProperties().getServiceRegistry().getCache().getCacheSize() > 0;
        if (cacheEnabled) {
            return fetchServicesFromCache();
        }
        return lock.tryLock(() -> {
            if (this.sortedRegisteredServices != null) {
                return this.sortedRegisteredServices;
            }

            this.sortedRegisteredServices = fetchServicesFromCache();
            return this.sortedRegisteredServices;
        });
    }

    private List<RegisteredService> fetchServicesFromCache() {
        return getCacheableServicesStream()
            .get()
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.toList());
    }
}

package org.apereo.cas.services.resource;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicePreDeleteEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesLoadedEvent;

import java.io.File;
import java.util.List;

/**
 * This is {@link DeleteResourceBasedRegisteredServiceWatcher}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class DeleteResourceBasedRegisteredServiceWatcher extends BaseResourceBasedRegisteredServiceWatcher {


    public DeleteResourceBasedRegisteredServiceWatcher(final AbstractResourceBasedServiceRegistryDao serviceRegistryDao) {
        super(serviceRegistryDao);
    }

    @Override
    public void accept(final File file) {
        LOGGER.debug("Service definition [{}] was deleted. Reloading cache...", file);
        final RegisteredService service = serviceRegistryDao.getRegisteredServiceFromFile(file);
        if (service != null) {
            serviceRegistryDao.publishEvent(new CasRegisteredServicePreDeleteEvent(this, service));
            serviceRegistryDao.removeRegisteredService(service);
            LOGGER.debug("Successfully deleted service definition [{}]", service.getName());
            serviceRegistryDao.publishEvent(new CasRegisteredServiceDeletedEvent(this, service));
        } else {
            LOGGER.warn("Unable to locate a matching service definition from file [{}]. Reloading cache...", file);
            final List<RegisteredService> results = serviceRegistryDao.load();
            serviceRegistryDao.publishEvent(new CasRegisteredServicesLoadedEvent(this, results));
        }
    }
}

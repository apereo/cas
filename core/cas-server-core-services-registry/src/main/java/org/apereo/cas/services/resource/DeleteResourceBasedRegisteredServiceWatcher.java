package org.apereo.cas.services.resource;

import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicePreDeleteEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesLoadedEvent;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.File;

/**
 * This is {@link DeleteResourceBasedRegisteredServiceWatcher}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class DeleteResourceBasedRegisteredServiceWatcher extends BaseResourceBasedRegisteredServiceWatcher {

    public DeleteResourceBasedRegisteredServiceWatcher(final AbstractResourceBasedServiceRegistry serviceRegistryDao) {
        super(serviceRegistryDao);
    }

    @Override
    public void accept(final File file) {
        LOGGER.debug("Service definition [{}] was deleted. Reloading cache...", file);
        val service = serviceRegistryDao.getRegisteredServiceFromFile(file);
        if (service != null) {
            serviceRegistryDao.publishEvent(new CasRegisteredServicePreDeleteEvent(this, service));
            serviceRegistryDao.removeRegisteredService(service);
            LOGGER.debug("Successfully deleted service definition [{}]", service.getName());
            serviceRegistryDao.publishEvent(new CasRegisteredServiceDeletedEvent(this, service));
        } else {
            LOGGER.warn("Unable to locate a matching service definition from file [{}]. Reloading cache...", file);
            val results = serviceRegistryDao.load();
            serviceRegistryDao.publishEvent(new CasRegisteredServicesLoadedEvent(this, results));
        }
    }
}

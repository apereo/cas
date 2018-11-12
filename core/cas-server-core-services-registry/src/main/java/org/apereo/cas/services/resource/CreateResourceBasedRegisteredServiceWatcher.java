package org.apereo.cas.services.resource;

import org.apereo.cas.support.events.service.CasRegisteredServicePreSaveEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.File;
import java.util.Objects;

/**
 * This is {@link CreateResourceBasedRegisteredServiceWatcher}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class CreateResourceBasedRegisteredServiceWatcher extends BaseResourceBasedRegisteredServiceWatcher {


    public CreateResourceBasedRegisteredServiceWatcher(final AbstractResourceBasedServiceRegistry serviceRegistryDao) {
        super(serviceRegistryDao);
    }

    @Override
    public void accept(final File file) {
        LOGGER.debug("New service definition [{}] was created. Locating service entry from cache...", file);
        val services = serviceRegistryDao.load(file);
        services.stream()
            .filter(Objects::nonNull)
            .forEach(service -> {
                if (serviceRegistryDao.findServiceById(service.getId()) != null) {
                    LOG_SERVICE_DUPLICATE.accept(service);
                }
                LOGGER.trace("Updating service definitions with [{}]", service);
                serviceRegistryDao.publishEvent(new CasRegisteredServicePreSaveEvent(this, service));
                serviceRegistryDao.update(service);
                serviceRegistryDao.publishEvent(new CasRegisteredServiceSavedEvent(this, service));
            });
    }
}

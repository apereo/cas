package org.apereo.cas.services.resource;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.service.CasRegisteredServicePreSaveEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.Objects;

/**
 * This is {@link CreateResourceBasedRegisteredServiceWatcher}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CreateResourceBasedRegisteredServiceWatcher extends BaseResourceBasedRegisteredServiceWatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateResourceBasedRegisteredServiceWatcher.class);
    
    public CreateResourceBasedRegisteredServiceWatcher(final AbstractResourceBasedServiceRegistryDao serviceRegistryDao) {
        super(serviceRegistryDao);
    }

    @Override
    public void accept(final File file) {
        LOGGER.debug("New service definition [{}] was created. Locating service entry from cache...", file);
        final Collection<RegisteredService> services = serviceRegistryDao.load(file);
        services.stream()
            .filter(Objects::nonNull)
            .forEach(service -> {
                if (serviceRegistryDao.findServiceById(service.getId()) != null) {
                    LOG_SERVICE_DUPLICATE.accept(service);
                }
                LOGGER.debug("Updating service definitions with [{}]", service);
                serviceRegistryDao.publishEvent(new CasRegisteredServicePreSaveEvent(this, service));
                serviceRegistryDao.update(service);
                serviceRegistryDao.publishEvent(new CasRegisteredServiceSavedEvent(this, service));
            });
    }
}

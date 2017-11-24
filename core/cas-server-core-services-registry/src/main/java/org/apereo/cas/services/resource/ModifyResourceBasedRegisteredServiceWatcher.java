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
 * This is {@link ModifyResourceBasedRegisteredServiceWatcher}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ModifyResourceBasedRegisteredServiceWatcher extends BaseResourceBasedRegisteredServiceWatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModifyResourceBasedRegisteredServiceWatcher.class);
    
    public ModifyResourceBasedRegisteredServiceWatcher(final AbstractResourceBasedServiceRegistryDao serviceRegistryDao) {
        super(serviceRegistryDao);
    }

    @Override
    public void accept(final File file) {
        LOGGER.debug("New service definition [{}] was modified. Locating service entry from cache...", file);
        final Collection<RegisteredService> newServices = serviceRegistryDao.load(file);
        newServices.stream()
            .filter(Objects::nonNull)
            .forEach(newService -> {
                final RegisteredService oldService = serviceRegistryDao.findServiceById(newService.getId());

                if (!newService.equals(oldService)) {
                    LOGGER.debug("Updating service definitions with [{}]", newService);
                    serviceRegistryDao.publishEvent(new CasRegisteredServicePreSaveEvent(this, newService));
                    serviceRegistryDao.update(newService);
                    serviceRegistryDao.publishEvent(new CasRegisteredServiceSavedEvent(this, newService));
                } else {
                    LOGGER.debug("Service [{}] loaded from [{}] is identical to the existing entry. Entry may have already been saved "
                        + "in the event processing pipeline", newService.getId(), file.getName());
                }
            });
    }
}

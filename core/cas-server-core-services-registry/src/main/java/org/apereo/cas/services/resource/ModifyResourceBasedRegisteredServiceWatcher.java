package org.apereo.cas.services.resource;

import module java.base;
import org.apereo.cas.support.events.service.CasRegisteredServicePreSaveEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;

/**
 * This is {@link ModifyResourceBasedRegisteredServiceWatcher}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class ModifyResourceBasedRegisteredServiceWatcher extends BaseResourceBasedRegisteredServiceWatcher {
    public ModifyResourceBasedRegisteredServiceWatcher(final AbstractResourceBasedServiceRegistry registry) {
        super(registry);
    }

    @Override
    public void accept(final File file) {
        val fileName = file.getName();
        if (!(!fileName.isEmpty() && fileName.charAt(0) == '.') && Arrays.stream(serviceRegistryDao.getExtensions()).anyMatch(fileName::endsWith)) {
            LOGGER.debug("New service definition [{}] was modified. Locating service entry from cache...", file);
            val newServices = serviceRegistryDao.load(file);
            val clientInfo = ClientInfoHolder.getClientInfo();
            newServices.stream()
                .filter(Objects::nonNull)
                .forEach(newService -> {
                    val oldService = serviceRegistryDao.findServiceById(newService.getId());

                    if (newService.equals(oldService)) {
                        LOGGER.debug("Service [{}] loaded from [{}] is identical to the existing entry. Entry may have already been saved "
                            + "in the event processing pipeline", newService.getId(), file.getName());
                    } else {
                        LOGGER.debug("Updating service definitions with [{}]", newService);
                        serviceRegistryDao.publishEvent(new CasRegisteredServicePreSaveEvent(this, newService, clientInfo));
                        serviceRegistryDao.update(newService);
                        serviceRegistryDao.publishEvent(new CasRegisteredServiceSavedEvent(this, newService, clientInfo));
                    }
                });
        }
    }
}

package org.apereo.cas.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


/**
 * Initializes a given service registry data store with available
 * JSON service definitions if necessary (based on configuration flag).
 *
 * @author Dmitriy Kopylenko
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class ServiceRegistryInitializer {
    private final ServiceRegistry jsonServiceRegistry;
    private final ServiceRegistry serviceRegistry;
    private final ServicesManager servicesManager;

    /**
     * Init service registry if necessary.
     */
    public void initServiceRegistryIfNecessary() {
        final long size = this.serviceRegistry.size();
        LOGGER.debug("Service registry contains [{}] service definition(s)", size);

        LOGGER.warn("Service registry [{}] will be auto-initialized from JSON service definitions. "
            + "This behavior is only useful for testing purposes and MAY NOT be appropriate for production. "
            + "Consider turning off this behavior via the setting [cas.serviceRegistry.initFromJson=false] "
            + "and explicitly register definitions in the services registry.", this.serviceRegistry.getName());

        final List<RegisteredService> servicesLoaded = this.jsonServiceRegistry.load();
        LOGGER.debug("Loading JSON services are [{}]", servicesLoaded);

        servicesLoaded.stream()
            .filter(s -> !findExistingMatchForService(s))
            .forEach(r -> {
                LOGGER.debug("Initializing service registry with the [{}] JSON service definition...", r);
                this.serviceRegistry.save(r);
            });
        this.servicesManager.load();
        LOGGER.info("Service registry [{}] contains [{}] service definitions", this.serviceRegistry.getName(), this.servicesManager.count());

    }

    private boolean findExistingMatchForService(final RegisteredService r) {
        RegisteredService match = this.serviceRegistry.findServiceById(r.getServiceId());
        if (match != null) {
            LOGGER.warn("Skipping [{}] JSON service definition as a matching service [{}] is found in the registry", r.getName(), match.getName());
            return true;
        }
        match = this.serviceRegistry.findServiceByExactServiceId(r.getServiceId());
        if (match != null) {
            LOGGER.warn("Skipping [{}] JSON service definition as a matching service [{}] is found in the registry", r.getName(), match.getName());
            return true;
        }
        match = this.serviceRegistry.findServiceById(r.getId());
        if (match != null) {
            LOGGER.warn("Skipping [{}] JSON service definition as a matching id [{}] is found in the registry", r.getName(), match.getId());
            return true;
        }
        return false;
    }
}

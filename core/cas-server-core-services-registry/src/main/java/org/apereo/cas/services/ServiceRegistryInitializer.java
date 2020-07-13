package org.apereo.cas.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Comparator;
import java.util.stream.Collectors;


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

    private final ChainingServiceRegistry serviceRegistry;

    private final ServicesManager servicesManager;

    /**
     * Init service registry if necessary.
     */
    public void initServiceRegistryIfNecessary() {
        LOGGER.debug("Total count of service registries is [{}] which contain [{}] service definition(s)",
            serviceRegistry.countServiceRegistries(), serviceRegistry.size());

        LOGGER.info("Service registries [{}] will be auto-initialized from JSON service definitions. "
            + "You can turn off this behavior via the setting [cas.service-registry.init-from-json=false] "
            + "and explicitly register definitions in the services registry.", serviceRegistry.getName());

        val servicesLoaded = jsonServiceRegistry.load();
        if (LOGGER.isDebugEnabled()) {
            val servicesList = servicesLoaded
                .stream()
                .map(RegisteredService::getName)
                .collect(Collectors.joining(","));
            LOGGER.debug("Loaded JSON service definitions are [{}]", servicesList);
        }

        servicesLoaded
            .stream()
            .sorted(Comparator.naturalOrder())
            .forEach(serviceRegistry::synchronize);
        this.servicesManager.load();
        LOGGER.info("Service registry [{}] contains [{}] service definitions",
            this.serviceRegistry.getName(), this.servicesManager.count());
    }
}

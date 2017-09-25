package org.apereo.cas.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * Initializes a given service registry data store with available
 * JSON service definitions if necessary (based on configuration flag).
 *
 * @author Dmitriy Kopylenko
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class ServiceRegistryInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistryInitializer.class);

    private final ServiceRegistryDao serviceRegistryDao;
    private final ServiceRegistryDao jsonServiceRegistryDao;
    private final ServicesManager servicesManager;
    private final boolean initFromJson;

    public ServiceRegistryInitializer(final ServiceRegistryDao jsonServiceRegistryDao,
                                      final ServiceRegistryDao serviceRegistryDao,
                                      final ServicesManager servicesManager,
                                      final boolean initFromJson) {
        this.jsonServiceRegistryDao = jsonServiceRegistryDao;
        this.serviceRegistryDao = serviceRegistryDao;
        this.servicesManager = servicesManager;
        this.initFromJson = initFromJson;
    }

    /**
     * Init service registry if necessary.
     */
    public void initServiceRegistryIfNecessary() {
        final long size = this.serviceRegistryDao.size();
        LOGGER.debug("Service registry contains [{}] service definitions", size);

        if (!this.initFromJson) {
            LOGGER.info("The service registry database backed by [{}] will not be initialized from JSON services. "
                    + "If the service registry database ends up empty, CAS will refuse to authenticate services "
                    + "until service definitions are added to the registry. To auto-initialize the service registry, "
                    + "set 'cas.serviceRegistry.initFromJson=true' in your CAS settings.",
                    this.serviceRegistryDao.getName());
            return;
        }

        LOGGER.warn("Service registry [{}] will be auto-initialized from JSON service definitions. "
                + "This behavior is only useful for testing purposes and MAY NOT be appropriate for production. "
                + "Consider turning off this behavior via the setting [cas.serviceRegistry.initFromJson=false] "
                + "and explicitly register definitions in the services registry.", this.serviceRegistryDao.getName());

        final List<RegisteredService> servicesLoaded = this.jsonServiceRegistryDao.load();
        LOGGER.debug("Loading JSON services are [{}]", servicesLoaded);

        for (final RegisteredService r : servicesLoaded) {
            if (findExistingMatchForService(r)) {
                continue;
            }
            LOGGER.debug("Initializing service registry with the [{}] JSON service definition...", r);
            this.serviceRegistryDao.save(r);
        }
        this.servicesManager.load();
        LOGGER.info("Service registry [{}] contains [{}] service definitions", this.serviceRegistryDao.getName(), this.servicesManager.count());

    }

    private boolean findExistingMatchForService(final RegisteredService r) {
        RegisteredService match = this.serviceRegistryDao.findServiceById(r.getServiceId());
        if (match != null) {
            LOGGER.warn("Skipping [{}] JSON service definition as a matching service [{}] is found in the registry", r.getName(), match.getName());
            return true;
        }
        match = this.serviceRegistryDao.findServiceByExactServiceId(r.getServiceId());
        if (match != null) {
            LOGGER.warn("Skipping [{}] JSON service definition as a matching service [{}] is found in the registry", r.getName(), match.getName());
            return true;
        }
        match = this.serviceRegistryDao.findServiceById(r.getId());
        if (match != null) {
            LOGGER.warn("Skipping [{}] JSON service definition as a matching id [{}] is found in the registry", r.getName(), match.getId());
            return true;
        }
        return false;
    }
}

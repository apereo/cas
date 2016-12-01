package org.apereo.cas.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;


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

    private ServiceRegistryDao serviceRegistryDao;

    private ServiceRegistryDao jsonServiceRegistryDao;

    private ServicesManager servicesManager;

    private boolean initFromJson;

    public ServiceRegistryInitializer() {
    }

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
    @PostConstruct
    public void initServiceRegistryIfNecessary() {
        final long size = this.serviceRegistryDao.size();
        LOGGER.debug("Service registry contains {} service definitions", size);


        if (this.initFromJson) {
            LOGGER.debug("Service registry will be auto-initialized from default JSON services");
            this.jsonServiceRegistryDao.load().forEach(r -> {
                if (this.serviceRegistryDao.findServiceById(r.getServiceId()) != null) {
                    LOGGER.debug("Skipping {} JSON service definition as a matching service is found in the registry", r.getName());
                } else if (this.serviceRegistryDao.findServiceById(r.getId()) != null) {
                    LOGGER.debug("Skipping {} JSON service definition as a matching numeric id is found in the registry", r.getName());
                } else {
                    LOGGER.debug("Initializing service registry with the {} JSON service definition...", r);
                    this.serviceRegistryDao.save(r);
                }
            });
            this.servicesManager.load();
            LOGGER.debug("Service registry contains {} service definitions", this.servicesManager.count());
        } else {
            LOGGER.info("The service registry database will not be initialized from default JSON services. "
                    + "If the service registry database ends up empty, CAS will refuse to authenticate services "
                    + "until service definitions are added to the registry.");
        }
    }
}

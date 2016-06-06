package org.jasig.cas;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServiceRegistryDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Initializes Jpa service registry database with available JSON service
 * definitions if necessary (based on configuration flag).
 *
 * @author Dmitriy Kopylenko
 * @author Misagh Moayyed
 * @since 4.2.3
 */
@Component("serviceRegistryInitializer")
class ServiceRegistryInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistryInitializer.class);

    private final ServiceRegistryDao serviceRegistryDao;

    private final ServiceRegistryDao jsonServiceRegistryDao;

    @Value("${svcreg.database.from.json:false}")
    private boolean initFromJsonIfAvailable;

    @Autowired
    ServiceRegistryInitializer(@Qualifier("jsonServiceRegistryDao")
                               final ServiceRegistryDao jsonServiceRegistryDao,
                               @Qualifier("serviceRegistryDao")
                               final ServiceRegistryDao serviceRegistryDao) {
        this.jsonServiceRegistryDao = jsonServiceRegistryDao;
        this.serviceRegistryDao = serviceRegistryDao;
    }

    /**
     * Init service registry if necessary.
     */
    @PostConstruct
    public void initServiceRegistryIfNecessary() {

        if (this.serviceRegistryDao.equals(this.jsonServiceRegistryDao)) {
            return;
        }

        if (this.initFromJsonIfAvailable) {
            final long size = this.serviceRegistryDao.load().size();
            if (size == 0) {
                LOGGER.debug("Service registry database will be auto-initialized from default JSON services");
                final List<RegisteredService> services = this.jsonServiceRegistryDao.load();
                for (final RegisteredService r : services) {
                    LOGGER.debug("Initializing DB with the {} JSON service definition...", r);
                    this.serviceRegistryDao.save(r);
                }
                this.serviceRegistryDao.load();
                LOGGER.info("The service registry database is  initialized from default JSON services.");
            } else {
                LOGGER.debug("Service registry database contains {} service definitions", size);
            }
        } else {
            LOGGER.info("The service registry database will not be initialized from default JSON services. "
                    + "If the service registry database is empty, CAS will refuse to authenticate services "
                    + "until service definitions are added to the database.");
        }


    }
}

package org.apereo.cas.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import javax.annotation.PostConstruct;


/**
 * Initializes Jpa service registry database with available JSON service definitions if necessary (based on configuration flag).
 *
 * @author Dmitriy Kopylenko
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("serviceRegistryInitializationConfiguration")
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
public class ServiceRegistryInitializationConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistryInitializationConfiguration.class);

    @Autowired
    @Qualifier("serviceRegistryDao")
    private ServiceRegistryDao serviceRegistryDao;

    @Autowired
    @Qualifier("jsonServiceRegistryDao")
    private ServiceRegistryDao jsonServiceRegistryDao;

    @Value("${svcreg.database.from.json:true}")
    private boolean initFromJsonIfAvailable;

    public ServiceRegistryInitializationConfiguration() {
    }

    /**
     * Init service registry if necessary.
     */
    @PostConstruct
    public void initServiceRegistryIfNecessary() {
        
        if (this.serviceRegistryDao.equals(this.jsonServiceRegistryDao)) {
            return;
        }
        
        final long size = this.serviceRegistryDao.size();
        
        if (size == 0) {
            if (this.initFromJsonIfAvailable) {
                LOGGER.debug("Service registry database will be auto-initialized from default JSON services");
                this.jsonServiceRegistryDao.load().forEach(r -> {
                    LOGGER.debug("Initializing DB with the {} JSON service definition...", r);
                    this.serviceRegistryDao.save(r);
                });
                this.serviceRegistryDao.load();
            } else {
                LOGGER.info("The service registry database will not be initialized from default JSON services. "
                        + "Since the service registry database is empty, CAS will refuse to authenticate services "
                        + "until service definitions are added to the database.");
            }
        } else {
            LOGGER.debug("Service registry database contains {} service definitions", size);
        }

    }
}

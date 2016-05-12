package org.apereo.cas.config;

import org.apereo.cas.services.ServiceRegistryDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


/**
 * Initializes Jpa service registry database with available JSON service definitions if necessary (based on configuration flag).
 *
 * This component is not a public API and is considered to be an internal CAS component. It is thus a package-private class. As such,
 * this class is not designed to be used outside of CAS itself and is a subject to change without any warnings.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Component
class JpaServiceRegistryInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaServiceRegistryInitializer.class);

    private ServiceRegistryDao jpaServiceRegistryDao;

    private ServiceRegistryDao jsonServiceRegistryDao;

    @Value("${svcreg.database.initFromJsonIfAvailable:true}")
    private boolean initFromJsonIfAvailable;

    @Autowired
    JpaServiceRegistryInitializer(@Qualifier("jpaServiceRegistryDao") final ServiceRegistryDao jpaServiceRegistryDao,
                                         @Qualifier("jsonServiceRegistryDao") final ServiceRegistryDao jsonServiceRegistryDao) {
        this.jpaServiceRegistryDao = jpaServiceRegistryDao;
        this.jsonServiceRegistryDao = jsonServiceRegistryDao;
    }

    @PostConstruct
    void initJpaServiceRegistryIfNecessary() {
        if(this.initFromJsonIfAvailable) {
            if(this.jpaServiceRegistryDao.size() == 0L) {
                LOGGER.debug("Service registry database is empty and configuration is set to init from default JSON services");
                this.jsonServiceRegistryDao.load().forEach(r -> {
                    LOGGER.debug("Initializing DB with the {} JSON service definition...", r);
                    this.jpaServiceRegistryDao.save(r);
                });
            }
        }
    }
}

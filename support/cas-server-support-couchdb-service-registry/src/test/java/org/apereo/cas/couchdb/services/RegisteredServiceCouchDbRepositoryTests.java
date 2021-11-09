package org.apereo.cas.couchdb.services;

import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.CouchDbServiceRegistryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceCouchDbRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCouchDbCoreConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreServicesConfiguration.class,
    CouchDbServiceRegistryConfiguration.class
},
    properties = {
        "cas.service-registry.couch-db.username=cas",
        "cas.service-registry.couch-db.caching=false",
        "cas.service-registry.couch-db.password=password"
    })
@Tag("CouchDb")
@EnabledIfPortOpen(port = 5984)
public class RegisteredServiceCouchDbRepositoryTests {
    @Autowired
    @Qualifier("serviceRegistryCouchDbRepository")
    private RegisteredServiceCouchDbRepository serviceRegistryCouchDbRepository;

    @Test
    public void verifyOperation() {
        assertNull(serviceRegistryCouchDbRepository.findByServiceName("unknown-service"));
        assertNull(serviceRegistryCouchDbRepository.get(554433));
    }

}

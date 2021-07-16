package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.CouchDbServiceRegistryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CouchDbServiceRegistryTests}.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCouchDbCoreConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CouchDbServiceRegistryConfiguration.class
},
    properties = {
        "cas.service-registry.couch-db.username=cas",
        "cas.service-registry.couch-db.caching=false",
        "cas.service-registry.couch-db.password=password"
    })
@Tag("CouchDb")
@EnabledIfPortOpen(port = 5984)
@Getter
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CouchDbServiceRegistryTests extends AbstractServiceRegistryTests {

    @Autowired
    @Qualifier("couchDbServiceRegistry")
    private ServiceRegistry newServiceRegistry;

    @Test
    public void verifyOperation() {
        assertNull(newServiceRegistry.findServiceByExactServiceName("unknown-service"));
        assertNull(newServiceRegistry.findServiceByExactServiceId("unknown-service"));
        assertNull(newServiceRegistry.findServiceById(554433));
    }

}

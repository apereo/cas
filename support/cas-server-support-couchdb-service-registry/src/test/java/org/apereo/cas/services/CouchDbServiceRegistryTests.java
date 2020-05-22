package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.CouchDbServiceRegistryConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

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
    CasCoreServicesConfiguration.class,
    CouchDbServiceRegistryConfiguration.class
},
    properties = {
        "cas.service-registry.couch-db.username=cas",
        "cas.service-registry.couch-db.password=password"
    })
@Tag("CouchDb")
@EnabledIfPortOpen(port = 5984)
public class CouchDbServiceRegistryTests extends AbstractServiceRegistryTests {

    @Autowired
    @Qualifier("couchDbServiceRegistry")
    private ServiceRegistry serviceRegistry;

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return this.serviceRegistry;
    }
}

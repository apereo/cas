package org.apereo.cas.services;

import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.CouchDbServiceRegistryConfiguration;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.services.RegisteredServiceCouchDbRepository;

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
    CasCouchDbCoreConfiguration.class,
    CouchDbServiceRegistryConfiguration.class
},
    properties = {
        "cas.serviceRegistry.couchDb.username=cas",
        "cas.serviceRegistry.couchDb.password=password"
    })
@Tag("couchdb")
public class CouchDbServiceRegistryTests extends AbstractServiceRegistryTests {

    @Autowired
    @Qualifier("couchDbServiceRegistry")
    private ServiceRegistry serviceRegistry;

    @Autowired
    @Qualifier("serviceRegistryCouchDbFactory")
    private CouchDbConnectorFactory couchDbFactory;

    @Autowired
    @Qualifier("serviceRegistryCouchDbRepository")
    private RegisteredServiceCouchDbRepository registeredServiceRepository;

    public CouchDbServiceRegistryTests() {
        super(RegexRegisteredService.class);
    }

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return this.serviceRegistry;
    }
}

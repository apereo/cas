package org.apereo.cas.services;

import org.apereo.cas.category.CouchDbCategory;
import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.CouchDbServiceRegistryConfiguration;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.services.RegisteredServiceCouchDbRepository;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.Collection;
import java.util.Collections;

/**
 * This is {@link CouchDbServiceRegistryTests}.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@RunWith(Parameterized.class)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCouchDbCoreConfiguration.class,
    CouchDbServiceRegistryConfiguration.class
},
    properties = {
        "cas.serviceRegistry.couchDb.username=cas",
        "cas.serviceRegistry.couchDb.password=password"
    })
@Category(CouchDbCategory.class)
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

    public CouchDbServiceRegistryTests(final Class<? extends RegisteredService> registeredServiceClass) {
        super(registeredServiceClass);
    }

    @Parameterized.Parameters
    public static Collection<Object> getTestParameters() {
        return Collections.singletonList(RegexRegisteredService.class);
    }

    @Override
    public void initializeServiceRegistry() {
        couchDbFactory.getCouchDbInstance().createDatabaseIfNotExists(couchDbFactory.getCouchDbConnector().getDatabaseName());
        registeredServiceRepository.initStandardDesignDocument();
        super.initializeServiceRegistry();
    }

    @Override
    public void tearDownServiceRegistry() {
        couchDbFactory.getCouchDbInstance().deleteDatabase(couchDbFactory.getCouchDbConnector().getDatabaseName());
        super.tearDownServiceRegistry();
    }

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return this.serviceRegistry;
    }
}

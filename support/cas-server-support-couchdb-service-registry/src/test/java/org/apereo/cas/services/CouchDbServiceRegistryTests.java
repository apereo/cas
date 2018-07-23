package org.apereo.cas.services;

import org.apereo.cas.category.CouchDbCategory;
import org.apereo.cas.config.CouchDbServiceRegistryConfiguration;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.services.RegisteredServiceRepository;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.Arrays;
import java.util.Collection;

/**
 * This is {@link CouchDbServiceRegistryTests}.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@RunWith(Parameterized.class)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CouchDbServiceRegistryConfiguration.class
},
    properties = {
        "org.ektorp.support.AutoUpdateViewOnChange=true",
        "cas.serviceRegistry.couchDb.username=",
        "cas.serviceRegistry.couchDb.password="
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
    private RegisteredServiceRepository registeredServiceRepository;

    public CouchDbServiceRegistryTests(final Class<? extends RegisteredService> registeredServiceClass) {
        super(registeredServiceClass);
    }

    @Parameterized.Parameters
    public static Collection<Object> getTestParameters() {
        return Arrays.asList(RegexRegisteredService.class);
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

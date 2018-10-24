package org.apereo.cas.services;

import org.apereo.cas.category.MongoDbCategory;
import org.apereo.cas.config.MongoDbServiceRegistryConfiguration;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.Collection;

/**
 * This is {@link MongoDbServiceRegistryCloudTests}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SpringBootTest(classes = {
    MongoDbServiceRegistryConfiguration.class,
    RefreshAutoConfiguration.class
})
@RunWith(Parameterized.class)
@Category(MongoDbCategory.class)
@EnabledIfContinuousIntegration
@EnabledIfPortOpen(port = 27017)
@TestPropertySource(properties = {
    "cas.serviceRegistry.mongo.databaseName=service-registry",
    "cas.serviceRegistry.mongo.host=localhost",
    "cas.serviceRegistry.mongo.port=27017",
    "cas.serviceRegistry.mongo.userId=root",
    "cas.serviceRegistry.mongo.password=secret",
    "cas.serviceRegistry.mongo.authenticationDatabaseName=admin",
    "cas.serviceRegistry.mongo.dropCollection=true"
})
public class MongoDbServiceRegistryCloudTests extends AbstractServiceRegistryTests {

    @Autowired
    @Qualifier("mongoDbServiceRegistry")
    private ServiceRegistry serviceRegistry;

    public MongoDbServiceRegistryCloudTests(final Class<? extends RegisteredService> registeredServiceClass) {
        super(registeredServiceClass);
    }

    @Parameterized.Parameters
    public static Collection<Object> getTestParameters() {
        return Arrays.asList(RegexRegisteredService.class);
    }

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return this.serviceRegistry;
    }
}

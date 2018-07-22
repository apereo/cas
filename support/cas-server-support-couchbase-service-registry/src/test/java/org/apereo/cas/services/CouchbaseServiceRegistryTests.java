package org.apereo.cas.services;

import org.apereo.cas.category.CouchbaseCategory;
import org.apereo.cas.config.CouchbaseServiceRegistryConfiguration;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

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
 * This is {@link CouchbaseServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CouchbaseServiceRegistryConfiguration.class
},
    properties = {
        "cas.serviceRegistry.couchbase.password=password",
        "cas.serviceRegistry.couchbase.bucket=testbucket"
    })
@Category(CouchbaseCategory.class)
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class)
@RunWith(Parameterized.class)
public class CouchbaseServiceRegistryTests extends AbstractServiceRegistryTests {

    @Autowired
    @Qualifier("couchbaseServiceRegistry")
    private ServiceRegistry serviceRegistry;

    public CouchbaseServiceRegistryTests(final Class<? extends RegisteredService> registeredServiceClass) {
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

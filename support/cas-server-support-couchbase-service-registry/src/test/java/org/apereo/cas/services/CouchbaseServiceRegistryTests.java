package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CouchbaseServiceRegistryConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;

/**
 * This is {@link CouchbaseServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SpringBootTest(classes = {
    CouchbaseServiceRegistryTests.CouchbaseServiceRegistryTestConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreUtilConfiguration.class,
    CouchbaseServiceRegistryConfiguration.class
},
    properties = {
        "cas.service-registry.couchbase.cluster-password=password",
        "cas.service-registry.couchbase.cluster-username=admin",
        "cas.service-registry.couchbase.bucket=testbucket"
    })
@Tag("Couchbase")
@EnabledIfPortOpen(port = 8091)
@Execution(ExecutionMode.SAME_THREAD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ResourceLock("Couchbase")
@Getter
public class CouchbaseServiceRegistryTests extends AbstractServiceRegistryTests {

    @Autowired
    @Qualifier("couchbaseServiceRegistry")
    private ServiceRegistry newServiceRegistry;

    @TestConfiguration("CouchbaseServiceRegistryTestConfiguration")
    @Lazy(false)
    public static class CouchbaseServiceRegistryTestConfiguration {

        @SneakyThrows
        @EventListener
        public void handleCouchbaseSaveEvent(final CouchbaseRegisteredServiceSavedEvent event) {
            Thread.sleep(100);
        }

        @SneakyThrows
        @EventListener
        public void handleCouchbaseDeleteEvent(final CouchbaseRegisteredServiceDeletedEvent event) {
            Thread.sleep(100);
        }
    }
}

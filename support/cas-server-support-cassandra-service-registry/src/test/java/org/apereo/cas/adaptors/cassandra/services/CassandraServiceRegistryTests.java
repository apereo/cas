package org.apereo.cas.adaptors.cassandra.services;

import org.apereo.cas.config.CassandraServiceRegistryAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.AbstractServiceRegistryTests;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CassandraServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    CassandraServiceRegistryAutoConfiguration.class,
    AbstractServiceRegistryTests.SharedTestConfiguration.class
},
    properties = {
        "cas.service-registry.cassandra.local-dc=datacenter1",
        "cas.service-registry.cassandra.keyspace=cas",
        "cas.service-registry.cassandra.ssl-protocols=TLSv1.2",
        "cas.http-client.host-name-verifier=none"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@Tag("Cassandra")
@ExtendWith(CasTestExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIfListeningOnPort(port = 9042)
@Getter
@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class,
    CassandraServiceRegistryTests.DisposingTestExecutionListener.class
})
class CassandraServiceRegistryTests extends AbstractServiceRegistryTests {
    @Autowired
    @Qualifier("cassandraServiceRegistry")
    private ServiceRegistry newServiceRegistry;

    @Test
    @Order(Integer.MAX_VALUE)
    void verifyFailOps() {
        assertNull(newServiceRegistry.save((RegisteredService) null));
        assertFalse(newServiceRegistry.delete(null));
    }

    static class DisposingTestExecutionListener implements TestExecutionListener {
        @Override
        public void afterTestClass(final TestContext testContext) throws Exception {
            var registry = testContext.getApplicationContext().getBean("cassandraServiceRegistry", ServiceRegistry.class);
            ((DisposableBean) registry).destroy();
        }
    }
}

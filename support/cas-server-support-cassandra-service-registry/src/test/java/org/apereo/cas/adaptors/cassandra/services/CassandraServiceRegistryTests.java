package org.apereo.cas.adaptors.cassandra.services;

import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CassandraServiceRegistryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.AbstractServiceRegistryTests;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CassandraServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    CassandraServiceRegistryConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreHttpConfiguration.class,
    RefreshAutoConfiguration.class
},
    properties = {
        "cas.service-registry.cassandra.local-dc=datacenter1",
        "cas.service-registry.cassandra.keyspace=cas"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@Tag("Cassandra")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIfPortOpen(port = 9042)
@Getter
public class CassandraServiceRegistryTests extends AbstractServiceRegistryTests {
    @Autowired
    @Qualifier("cassandraServiceRegistry")
    private ServiceRegistry newServiceRegistry;

    @Test
    public void verifyFailOps() throws Exception {
        assertNull(newServiceRegistry.save((RegisteredService) null));
        assertFalse(newServiceRegistry.delete(null));
        if (newServiceRegistry instanceof DisposableBean) {
            DisposableBean.class.cast(newServiceRegistry).destroy();
        }
    }

}

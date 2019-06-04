package org.apereo.cas.adaptors.cassandra.services;

import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CassandraServiceRegistryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.AbstractServiceRegistryTests;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CassandraServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    CassandraServiceRegistryConfiguration.class,
    CasCoreServicesConfiguration.class,
    RefreshAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@TestPropertySource(properties = {"cas.serviceRegistry.cassandra.keyspace=cas"})
@Tag("Cassandra")
@EnabledIfContinuousIntegration
public class CassandraServiceRegistryTests extends AbstractServiceRegistryTests {
    @Autowired
    @Qualifier("cassandraServiceRegistry")
    private ServiceRegistry dao;

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return this.dao;
    }
}

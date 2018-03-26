package org.apereo.cas.services;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.JpaCoreConfiguration;
import org.apereo.cas.config.JpaJdbcConfiguration;
import org.apereo.cas.config.JpaNoSqlConfiguration;
import org.apereo.cas.config.JpaServiceRegistryConfiguration;
import org.apereo.cas.config.support.EnvironmentConversionServiceInitializer;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;


/**
 * Handles tests for {@link JpaServiceRegistry} via Cassandra.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    AopAutoConfiguration.class,
    CasCoreUtilConfiguration.class,
    JpaNoSqlConfiguration.class,
    JpaJdbcConfiguration.class,
    JpaCoreConfiguration.class,
    JpaServiceRegistryConfiguration.class,
    JpaServiceRegistryTests.TimeAwareServicesManagerConfiguration.class,
    CasCoreServicesConfiguration.class})
@DirtiesContext
@ContextConfiguration(initializers = {EnvironmentConversionServiceInitializer.class})
@TestPropertySource(properties = {"cas.ticket.registry.jpa.type=NOSQL", "cas.ticket.registry.jpa.provider=org.hibernate.ogm.datastore.cassandra.impl.CassandraDatastoreProvider",
    "cas.ticket.registry.jpa.host=127.0.0.1:9042"})
@Slf4j
public class CassandraJpaServiceRegistryTests extends JpaServiceRegistryTests {
}

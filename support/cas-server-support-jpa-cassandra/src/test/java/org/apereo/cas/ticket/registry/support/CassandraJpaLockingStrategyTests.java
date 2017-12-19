package org.apereo.cas.ticket.registry.support;

import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.JpaCoreConfiguration;
import org.apereo.cas.config.JpaJdbcConfiguration;
import org.apereo.cas.config.JpaNoSqlConfiguration;
import org.apereo.cas.config.JpaTicketRegistryConfiguration;
import org.apereo.cas.config.JpaTicketRegistryTicketCatalogConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Unit test for {@link JpaLockingStrategy} via Cassandra.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    AopAutoConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    JpaNoSqlConfiguration.class,
    JpaJdbcConfiguration.class,
    JpaCoreConfiguration.class,
    JpaTicketRegistryTicketCatalogConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    JpaTicketRegistryConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class})
@TestPropertySource(properties = {"cas.ticket.registry.jpa.type=NOSQL", "cas.ticket.registry.jpa.provider=org.hibernate.ogm.datastore.cassandra.impl.CassandraDatastoreProvider",
    "cas.ticket.registry.jpa.host=127.0.0.1:9042"})
public class CassandraJpaLockingStrategyTests extends JpaLockingStrategyTests{
}

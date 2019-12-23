package org.apereo.cas.ticket.registry;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import lombok.val;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.jpa.ticketregistry.JpaTicketRegistryProperties;
import org.apereo.cas.memcached.kryo.CasKryoPool;
import org.apereo.cas.ticket.DefaultTicketCatalog;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.expiration.TicketGrantingTicketExpirationPolicy;
import org.apereo.cas.ticket.registry.BaseTicketRegistryTests;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.DigestUtils;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.Arrays;

@SpringBootTest(classes = { CasCoreTicketCatalogConfiguration.class,
        JdbcTicketRegistryTest.JdbcRegistryTestConfiguration.class
})
public class JdbcTicketRegistryTest extends BaseTicketRegistryTests {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    TransactionManager transactionManager;

    @Autowired
    JpaTicketRegistryProperties jpaTicketRegistryProperties;

    @Autowired
    CasKryoPool casKryoPool;

    @Autowired
    CasCoreTicketCatalogConfiguration ticketCatalogConfiguration;

    @Override
    protected TicketRegistry getNewTicketRegistry() {
        val ticketCatalog = new DefaultTicketCatalog();
        val transactionTemplate = new TransactionTemplate((PlatformTransactionManager) transactionManager);

        ticketCatalogConfiguration.configureTicketCatalog(ticketCatalog);

        val meterRegistry = new CompositeMeterRegistry(Clock.SYSTEM);
        val jdbcTicketRegistry = new JdbcTicketRegistry(
                ticketCatalog,
                jdbcTemplate,
                transactionTemplate,
                jpaTicketRegistryProperties,
                meterRegistry,
                casKryoPool
        );

        val crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();
        jdbcTicketRegistry.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(crypto, "jpa"));

        jdbcTicketRegistry.initDatastore();
        return jdbcTicketRegistry;
    }

    @Override
    public void verifyTicketCountsEqualToTicketsAdded() {
        // Ignore this test, get ticket count is not implemented
    }

    @Override
    public void verifyGetTicketsFromRegistryEqualToTicketsAdded() {
        // Ignore this test, get ticket count is not implemented
    }

    @Override
    public void verifyDeleteTicketsWithMultiplePGTs() {
        // Ignore this test because we don't use PGTs.
        // Be aware that this test would fail on the second run; this may or may not indicate a problem
        // with our specific use cases.
    }

    @Override
    public void verifyDeleteTicketWithPGT() {
        // Ignore this test because we don't use PGTs.
        // Be aware that this test would fail on the second run; this may or may not indicate a problem
        // with our specific use cases.
    }

    @RepeatedTest(1)
    public void testDeleteByPrincipal() {

        final JdbcTicketRegistry registry = (JdbcTicketRegistry) getNewTicketRegistry();

        final String tgtId = "TGT-ID1";
        registry.addTicket(new TicketGrantingTicketImpl(tgtId, CoreAuthenticationTestUtils
                .getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE));

        Assertions.assertNotNull(registry.getTicket(tgtId));

        registry.deleteByPrincipalId(CoreAuthenticationTestUtils.getPrincipal().getId());

        Assertions.assertNull(registry.getTicket(tgtId));

    }

    @RepeatedTest(1)
    public void testExpirationField() {
        final JdbcTicketRegistry registry = (JdbcTicketRegistry) getNewTicketRegistry();

        final String tgtId = "TGT-ID1-EXP";
        val oneYearSeconds = 365 * 24 * 60 * 60L;
        val expirationPolicy = new TicketGrantingTicketExpirationPolicy(oneYearSeconds, 60);
        registry.addTicket(new TicketGrantingTicketImpl(tgtId, CoreAuthenticationTestUtils
                .getAuthentication(), expirationPolicy));
        // we record the time near where insert happens so can validate the expiration in the db one year plus this
        val currentTime = System.currentTimeMillis();

        Assertions.assertNotNull(registry.getTicket(tgtId));

        val encodedId = DigestUtils.sha512(tgtId);
        val expiration = jdbcTemplate.queryForObject("SELECT expiration FROM TICKETGRANTINGTICKET WHERE id=?", Long.class, encodedId);

        val diff = Math.abs(expiration - currentTime - (oneYearSeconds * 1000));
        // Since the currentTime recorded will not be the exact same as the one in JdbcTicketRegistry we need to give some tolerance
        val tolerance = 30 * 1000L;
        org.assertj.core.api.Assertions.assertThat(diff).isCloseTo(0L, Offset.offset(tolerance));
    }

    @Configuration
    @EnableTransactionManagement
    public static class JdbcRegistryTestConfiguration {

        @Bean
        public JpaTicketRegistryProperties jpaTicketRegistryProperties() {
            return new JpaTicketRegistryProperties();
        }

        @Bean
        public CasKryoPool casKryoPool() {
            return new CasKryoPool(Arrays.asList(
                    TicketGrantingTicketImpl.class,
                    ServiceTicketImpl.class));
        }

        @Bean
        public DataSource getDataSource() {
            DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
            dataSourceBuilder.driverClassName("org.hsqldb.jdbc.JDBCDriver");
            dataSourceBuilder.url("jdbc:hsqldb:mem:test");
            dataSourceBuilder.username("SA");
            dataSourceBuilder.password("");

            return dataSourceBuilder.build();
        }

        @Bean
        public JdbcTemplate getJdbcTemplate() {
            return new JdbcTemplate(getDataSource());
        }

        @Bean
        TransactionManager transactionManager() {
            return new DataSourceTransactionManager(getDataSource());
        }
    }

}

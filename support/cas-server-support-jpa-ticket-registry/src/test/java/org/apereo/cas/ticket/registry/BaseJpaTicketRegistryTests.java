package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreSamlAutoConfiguration;
import org.apereo.cas.config.CasHibernateJpaAutoConfiguration;
import org.apereo.cas.config.CasJpaTicketRegistryAutoConfiguration;
import org.apereo.cas.config.CasOAuth20AutoConfiguration;
import org.apereo.cas.config.CasWsSecuritySecurityTokenAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CloseableDataSource;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.DefaultSecurityTokenTicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.code.OAuth20CodeFactory;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.TicketGrantingTicketIdGenerator;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.integration.autoconfigure.IntegrationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link JpaTicketRegistry} class.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@Import(BaseJpaTicketRegistryTests.SharedTestConfiguration.class)
@TestPropertySource(
    properties = {
        "cas.jdbc.show-sql=false",
        "cas.ticket.registry.jpa.ddl-auto=create-drop"
    })
@Tag("JDBC")
@Getter
@EnableConfigurationProperties({IntegrationProperties.class, CasConfigurationProperties.class})
public abstract class BaseJpaTicketRegistryTests extends BaseTicketRegistryTests {
    private static final int COUNT = 500;

    @Autowired
    @Qualifier("defaultOAuthCodeFactory")
    protected OAuth20CodeFactory oAuthCodeFactory;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    protected TicketRegistry newTicketRegistry;

    @Autowired
    @Qualifier("dataSourceTicket")
    protected CloseableDataSource dataSourceTicket;

    @AfterEach
    public void cleanup() {
        assertNotNull(dataSourceTicket);
        newTicketRegistry.deleteAll();
    }

    @RepeatedTest(2)
    void verifyLargeDataset() {
        val ticketGrantingTickets = Stream.generate(() -> {
            val tgtId = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                .getNewTicketId(TicketGrantingTicket.PREFIX);
            return new TicketGrantingTicketImpl(tgtId,
                CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        }).limit(COUNT);

        val stopwatch = new StopWatch();
        stopwatch.start();
        newTicketRegistry.addTicket(ticketGrantingTickets);

        assertEquals(COUNT, newTicketRegistry.getTickets().size());
        stopwatch.stop();
        val time = stopwatch.getTime(TimeUnit.SECONDS);
        assertTrue(time <= 20);
    }

    @RepeatedTest(2)
    void verifySecurityTokenTicket() throws Throwable {
        val securityTokenTicketFactory = new DefaultSecurityTokenTicketFactory(
            new DefaultUniqueTicketIdGenerator(),
            neverExpiresExpirationPolicyBuilder());

        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        val ticketGrantingTicketId = TestTicketIdentifiers.generate().ticketGrantingTicketId();
        val tgt = new TicketGrantingTicketImpl(ticketGrantingTicketId,
            originalAuthn, NeverExpiresExpirationPolicy.INSTANCE);
        newTicketRegistry.addTicket(tgt);

        val token = securityTokenTicketFactory.create(tgt, "dummy-token".getBytes(StandardCharsets.UTF_8));
        newTicketRegistry.addTicket(token);

        assertNotNull(newTicketRegistry.getTicket(token.getId()));
        newTicketRegistry.deleteTicket(token);
        assertNull(newTicketRegistry.getTicket(token.getId()));
    }

    @RepeatedTest(2)
    void verifyLogoutCascades() throws Throwable {
        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        val tgtFactory = (TicketGrantingTicketFactory) ticketFactory.get(TicketGrantingTicket.class);
        val tgt = tgtFactory.create(RegisteredServiceTestUtils.getAuthentication(),
            RegisteredServiceTestUtils.getService());
        newTicketRegistry.addTicket(tgt);

        val oAuthCode = oAuthCodeFactory.create(RegisteredServiceTestUtils.getService(),
            originalAuthn, tgt, Collections.emptySet(),
            "challenge", "challenge_method",
            "client_id", Collections.emptyMap(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);

        newTicketRegistry.addTicket(oAuthCode);

        assertNotNull(newTicketRegistry.getTicket(oAuthCode.getId()));
        newTicketRegistry.deleteTicket(tgt.getId());
        assertNull(newTicketRegistry.getTicket(oAuthCode.getId()));
    }

    @RepeatedTest(2)
    @Transactional(transactionManager = TicketRegistry.TICKET_TRANSACTION_MANAGER, readOnly = false)
    void verifyRegistryQuery() throws Throwable {
        val tgt = new TicketGrantingTicketImpl("TGT-335500",
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        val registry = getNewTicketRegistry();
        registry.addTicket(tgt);
        assertEquals(1, registry.query(TicketRegistryQueryCriteria.builder()
            .count(1L).type(TicketGrantingTicket.PREFIX).decode(true).build()).size());
    }

    @Import(BaseTicketRegistryTests.SharedTestConfiguration.class)
    @ImportAutoConfiguration({
        CasJpaTicketRegistryAutoConfiguration.class,
        CasHibernateJpaAutoConfiguration.class,
        CasWsSecuritySecurityTokenAutoConfiguration.class,
        CasCoreSamlAutoConfiguration.class,
        CasOAuth20AutoConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}

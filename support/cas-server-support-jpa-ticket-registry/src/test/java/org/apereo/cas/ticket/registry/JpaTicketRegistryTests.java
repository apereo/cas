package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.config.CasOAuth20Configuration;
import org.apereo.cas.config.CasOAuth20EndpointsConfiguration;
import org.apereo.cas.config.CasOAuth20TicketSerializationConfiguration;
import org.apereo.cas.config.CasWsSecurityTokenTicketCatalogConfiguration;
import org.apereo.cas.config.CasWsSecurityTokenTicketComponentSerializationConfiguration;
import org.apereo.cas.config.JpaTicketRegistryConfiguration;
import org.apereo.cas.config.JpaTicketRegistryTicketCatalogConfiguration;
import org.apereo.cas.config.OAuth20ProtocolTicketCatalogConfiguration;
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
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link JpaTicketRegistry} class.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@SpringBootTest(classes = JpaTicketRegistryTests.SharedTestConfiguration.class,
    properties = {
        "cas.jdbc.show-sql=false",
        "cas.ticket.registry.jpa.ddl-auto=create-drop"
    })
@Tag("JDBC")
@Getter
@DirtiesContext
public class JpaTicketRegistryTests extends BaseTicketRegistryTests {
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

    @AfterAll
    public static void afterAllTests() throws Exception {
        ApplicationContextProvider.getApplicationContext()
            .getBean("dataSourceTicket", CloseableDataSource.class).close();
    }

    @AfterEach
    public void cleanup() throws Exception {
        assertNotNull(dataSourceTicket);
        newTicketRegistry.deleteAll();
    }

    @RepeatedTest(2)
    public void verifyLargeDataset() {
        val ticketGrantingTickets = Stream.generate(() -> {
            var ticketGrantingTicketId = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                .getNewTicketId(TicketGrantingTicket.PREFIX);
            return new TicketGrantingTicketImpl(ticketGrantingTicketId,
                CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        }).limit(COUNT);

        var stopwatch = new StopWatch();
        stopwatch.start();
        newTicketRegistry.addTicket(ticketGrantingTickets);

        assertEquals(COUNT, newTicketRegistry.getTickets().size());
        stopwatch.stop();
        var time = stopwatch.getTime(TimeUnit.SECONDS);
        assertTrue(time <= 20);
    }

    @RepeatedTest(2)
    public void verifySecurityTokenTicket() {
        val securityTokenTicketFactory = new DefaultSecurityTokenTicketFactory(
            new DefaultUniqueTicketIdGenerator(),
            neverExpiresExpirationPolicyBuilder());

        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        val tgt = new TicketGrantingTicketImpl(ticketGrantingTicketId,
            originalAuthn, NeverExpiresExpirationPolicy.INSTANCE);
        this.newTicketRegistry.addTicket(tgt);

        val token = securityTokenTicketFactory.create(tgt, "dummy-token".getBytes(StandardCharsets.UTF_8));
        this.newTicketRegistry.addTicket(token);

        assertNotNull(this.newTicketRegistry.getTicket(token.getId()));
        this.newTicketRegistry.deleteTicket(token);
        assertNull(this.newTicketRegistry.getTicket(token.getId()));
    }

    @RepeatedTest(2)
    public void verifyLogoutCascades() {
        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        val tgtFactory = (TicketGrantingTicketFactory) ticketFactory.get(TicketGrantingTicket.class);
        val tgt = tgtFactory.create(RegisteredServiceTestUtils.getAuthentication(),
            RegisteredServiceTestUtils.getService(), TicketGrantingTicket.class);
        this.newTicketRegistry.addTicket(tgt);

        val oAuthCode = oAuthCodeFactory.create(RegisteredServiceTestUtils.getService(),
            originalAuthn, tgt, Collections.emptySet(), "challenge", "challenge_method",
            "client_id", Collections.emptyMap(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);

        this.newTicketRegistry.addTicket(oAuthCode);

        assertNotNull(this.newTicketRegistry.getTicket(oAuthCode.getId()));
        this.newTicketRegistry.deleteTicket(tgt.getId());
        assertNull(this.newTicketRegistry.getTicket(oAuthCode.getId()));
    }

    @ImportAutoConfiguration({
        AopAutoConfiguration.class,
        RefreshAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        JpaTicketRegistryTicketCatalogConfiguration.class,
        JpaTicketRegistryConfiguration.class,
        CasHibernateJpaConfiguration.class,
        BaseTicketRegistryTests.SharedTestConfiguration.class,
        CasWsSecurityTokenTicketComponentSerializationConfiguration.class,
        CasWsSecurityTokenTicketCatalogConfiguration.class,
        CasOAuth20Configuration.class,
        CasOAuth20TicketSerializationConfiguration.class,
        CasOAuth20EndpointsConfiguration.class,
        OAuth20ProtocolTicketCatalogConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}

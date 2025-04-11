package org.apereo.cas.ticket.registry.cleaner;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketFactory;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenFactory;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.code.OAuth20DefaultOAuthCodeFactory;
import org.apereo.cas.ticket.device.OAuth20DeviceToken;
import org.apereo.cas.ticket.device.OAuth20DeviceTokenFactory;
import org.apereo.cas.ticket.device.OAuth20DeviceUserCode;
import org.apereo.cas.ticket.device.OAuth20DeviceUserCodeFactory;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.BaseJpaTicketRegistryTests;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.RetryingTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.integration.IntegrationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link BaseJpaTicketRegistryCleanerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = BaseJpaTicketRegistryTests.SharedTestConfiguration.class,
    properties = {
        "spring.integration.jdbc.initialize-schema=ALWAYS",
        "cas.ticket.registry.jpa.ddl-auto=create-drop"
    })
@EnableConfigurationProperties({IntegrationProperties.class, CasConfigurationProperties.class})
@ExtendWith(CasTestExtension.class)
public abstract class BaseJpaTicketRegistryCleanerTests {

    @Autowired
    @Qualifier(TicketTrackingPolicy.BEAN_NAME_SERVICE_TICKET_TRACKING)
    protected TicketTrackingPolicy serviceTicketSessionTrackingPolicy;

    @Autowired
    @Qualifier(TicketFactory.BEAN_NAME)
    protected TicketFactory ticketFactory;

    @Autowired
    @Qualifier("defaultAccessTokenFactory")
    protected OAuth20AccessTokenFactory accessTokenFactory;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier(TicketRegistryCleaner.BEAN_NAME)
    private TicketRegistryCleaner ticketRegistryCleaner;

    @BeforeEach
    void cleanup() {
        ticketRegistry.deleteAll();
    }

    @Test
    void verifyOperation() throws Throwable {
        val tgtFactory = (TicketGrantingTicketFactory) ticketFactory.get(TicketGrantingTicket.class);
        val tgt = tgtFactory.create(RegisteredServiceTestUtils.getAuthentication(),
            RegisteredServiceTestUtils.getService());
        ticketRegistry.addTicket(tgt);

        val stFactory = (ServiceTicketFactory) ticketFactory.get(ServiceTicket.class);
        val st = stFactory.create(tgt, RegisteredServiceTestUtils.getService(), true, ServiceTicket.class);

        ticketRegistry.addTicket(st);
        ticketRegistry.updateTicket(tgt);

        assertEquals(1, ticketRegistry.sessionCount());
        assertEquals(1, ticketRegistry.serviceTicketCount());

        st.markTicketExpired();
        tgt.markTicketExpired();

        ticketRegistry.updateTicket(st);
        ticketRegistry.updateTicket(tgt);

        assertTrue(ticketRegistryCleaner.clean() > 0);

        assertEquals(0, ticketRegistry.sessionCount());
        assertEquals(0, ticketRegistry.serviceTicketCount());
    }

    @Test
    void verifyTransientTicketCleaning() throws Throwable {
        val tgtFactory = (TicketGrantingTicketFactory) ticketFactory.get(TicketGrantingTicket.class);
        val tgt = tgtFactory.create(RegisteredServiceTestUtils.getAuthentication(),
            RegisteredServiceTestUtils.getService());
        ticketRegistry.addTicket(tgt);

        val transientFactory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);
        val transientTicket = transientFactory.create(RegisteredServiceTestUtils.getService());
        ticketRegistry.addTicket(transientTicket);

        ticketRegistry.updateTicket(tgt);

        transientTicket.markTicketExpired();
        tgt.markTicketExpired();

        ticketRegistry.updateTicket(transientTicket);
        ticketRegistry.updateTicket(tgt);

        assertEquals(2, ticketRegistry.getTickets().size());
        assertEquals(2, ticketRegistryCleaner.clean());
        assertTrue(ticketRegistry.getTickets().isEmpty());
    }

    @RepeatedTest(2)
    void verifyOauthOperation() throws Throwable {
        val tgtFactory = (TicketGrantingTicketFactory) ticketFactory.get(TicketGrantingTicket.class);
        val tgt = tgtFactory.create(RegisteredServiceTestUtils.getAuthentication(),
            RegisteredServiceTestUtils.getService());
        ticketRegistry.addTicket(tgt);

        val code = createOAuthCode();
        val at = accessTokenFactory.create(RegisteredServiceTestUtils.getService(),
            RegisteredServiceTestUtils.getAuthentication(), tgt,
            Collections.singleton("scope1"), code.getId(), "client1", Collections.emptyMap(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);

        ticketRegistry.addTicket(at);
        ticketRegistry.updateTicket(tgt);

        assertEquals(1, ticketRegistry.sessionCount());
        assertNotNull(ticketRegistry.getTicket(at.getId()));

        at.markTicketExpired();
        tgt.markTicketExpired();

        ticketRegistry.updateTicket(at);
        ticketRegistry.updateTicket(tgt);

        assertEquals(2, ticketRegistryCleaner.clean());
        assertEquals(0, ticketRegistry.sessionCount());
        assertNull(ticketRegistry.getTicket(at.getId()));
    }

    @Test
    void verifyDeviceCodeAndUserCleaning() throws Throwable {
        val tgtFactory = (TicketGrantingTicketFactory) ticketFactory.get(TicketGrantingTicket.class);
        val tgt = tgtFactory.create(RegisteredServiceTestUtils.getAuthentication(),
            RegisteredServiceTestUtils.getService());
        ticketRegistry.addTicket(tgt);

        val deviceCodeFactory = (OAuth20DeviceTokenFactory) ticketFactory.get(OAuth20DeviceToken.class);
        val deviceCode = deviceCodeFactory.createDeviceCode(RegisteredServiceTestUtils.getService());
        ticketRegistry.addTicket(deviceCode);

        val deviceUserCodeFactory = (OAuth20DeviceUserCodeFactory) ticketFactory.get(OAuth20DeviceUserCode.class);
        val deviceUserCode = deviceUserCodeFactory.createDeviceUserCode(deviceCode.getService());
        ticketRegistry.addTicket(deviceUserCode);

        ticketRegistry.updateTicket(tgt);

        deviceCode.markTicketExpired();
        deviceUserCode.markTicketExpired();
        tgt.markTicketExpired();

        ticketRegistry.updateTicket(deviceCode);
        ticketRegistry.updateTicket(deviceUserCode);
        ticketRegistry.updateTicket(tgt);

        assertEquals(3, ticketRegistry.getTickets().size());
        assertEquals(3, ticketRegistryCleaner.clean());
        assertTrue(ticketRegistry.getTickets().isEmpty());
    }

    @RetryingTest(2)
    void verifyConcurrentCleaner() throws Throwable {
        val registryTask = new TimerTask() {
            @Override
            public void run() {
                for (var i = 0; i < 5; i++) {
                    FunctionUtils.doUnchecked(__ -> {
                        val tgt = new TicketGrantingTicketImpl(TicketGrantingTicket.PREFIX + '-' + RandomUtils.randomAlphabetic(16),
                            CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString()),
                            new HardTimeoutExpirationPolicy(1));
                        ticketRegistry.addTicket(tgt);

                        val st = tgt.grantServiceTicket(
                            ServiceTicket.PREFIX + '-' + RandomUtils.randomAlphabetic(16), RegisteredServiceTestUtils.getService(),
                            new HardTimeoutExpirationPolicy(1), true, serviceTicketSessionTrackingPolicy);
                        ticketRegistry.addTicket(st);
                        ticketRegistry.updateTicket(tgt);
                    });
                }
            }
        };
        val registryTimer = new Timer("TicketRegistry");
        registryTimer.scheduleAtFixedRate(registryTask, 5, 5);


        val cleanerTask = new TimerTask() {
            @Override
            public void run() {
                ticketRegistryCleaner.clean();
            }
        };
        val cleanerTimer = new Timer("TicketRegistryCleanerTimer");
        cleanerTimer.scheduleAtFixedRate(cleanerTask, 10, 5);

        Thread.sleep(1000 * 15);
        cleanerTimer.cancel();
        registryTimer.cancel();
        ticketRegistry.deleteAll();
    }

    private OAuth20Code createOAuthCode() throws Throwable {
        val builder = mock(ExpirationPolicyBuilder.class);
        when(builder.buildTicketExpirationPolicy()).thenReturn(NeverExpiresExpirationPolicy.INSTANCE);

        return new OAuth20DefaultOAuthCodeFactory(new DefaultUniqueTicketIdGenerator(),
            builder, servicesManager, CipherExecutor.noOpOfStringToString(), TicketTrackingPolicy.noOp())
            .create(RegisteredServiceTestUtils.getService(),
                RegisteredServiceTestUtils.getAuthentication(), new MockTicketGrantingTicket("casuser"),
                CollectionUtils.wrapSet("1", "2"), "code-challenge",
                "code-challenge-method", "clientId1234567", new HashMap<>(),
                OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
    }
}

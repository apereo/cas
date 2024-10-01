package org.apereo.cas.ticket.registry;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasMemcachedTicketRegistryAutoConfiguration;
import org.apereo.cas.config.CasOAuth20AutoConfiguration;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.code.OAuth20DefaultOAuthCodeFactory;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.serialization.ComponentSerializationPlan;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import lombok.Getter;
import lombok.val;
import net.spy.memcached.MemcachedClientIF;
import org.apache.commons.pool2.ObjectPool;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for MemcachedTicketRegistry class.
 *
 * @author Middleware Services
 * @since 3.0.0
 * @deprecated Since 7.0.0
 */
@ImportAutoConfiguration({
    CasMemcachedTicketRegistryAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasOAuth20AutoConfiguration.class
})
@Import({
    CasAuthenticationEventExecutionPlanTestConfiguration.class,
    MemcachedTicketRegistryTests.MemcachedTicketRegistryTestConfiguration.class
})
@TestPropertySource(
    properties = {
        "cas.ticket.registry.memcached.servers=localhost:11211",
        "cas.ticket.registry.memcached.failure-mode=Redistribute",
        "cas.ticket.registry.memcached.locator-type=ARRAY_MOD",
        "cas.ticket.registry.memcached.transcoder=KRYO",
        "cas.ticket.registry.memcached.hash-algorithm=FNV1A_64_HASH",
        "cas.ticket.registry.memcached.kryo-registration-required=true"
    })
@EnabledIfListeningOnPort(port = 11211)
@Tag("Memcached")
@Getter
@Deprecated(since = "7.0.0")
class MemcachedTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier(CentralAuthenticationService.BEAN_NAME)
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Override
    protected boolean canTicketRegistryIterate() {
        return false;
    }

    @RepeatedTest(1)
    void verifyCreatePgt() throws Throwable {
        val tgt = new MockTicketGrantingTicket("casuser");
        newTicketRegistry.addTicket(tgt);
        val service = RegisteredServiceTestUtils.getService();

        val serviceTicketId = TestTicketIdentifiers.generate().serviceTicketId();
        val st = new MockServiceTicket(serviceTicketId, service, tgt);
        newTicketRegistry.addTicket(st);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(new HashMap());
        servicesManager.save(registeredService);

        centralAuthenticationService.createProxyGrantingTicket(st.getId(), CoreAuthenticationTestUtils.getAuthenticationResult(service));
        assertEquals(0, tgt.getProxyGrantingTickets().size());
        val tgt2 = newTicketRegistry.getTicket(tgt.getId(), TicketGrantingTicket.class);
        assertEquals(1, tgt2.getProxyGrantingTickets().size());
    }

    @RepeatedTest(2)
    void verifyOAuthCodeIsAddedToMemcached() throws Throwable {
        val factory = new OAuth20DefaultOAuthCodeFactory(new DefaultUniqueTicketIdGenerator(),
            neverExpiresExpirationPolicyBuilder(), servicesManager, CipherExecutor.noOpOfStringToString(),
            TicketTrackingPolicy.noOp());
        val code = factory.create(RegisteredServiceTestUtils.getService(),
            CoreAuthenticationTestUtils.getAuthentication(),
            new MockTicketGrantingTicket("casuser"),
            CollectionUtils.wrapList("openid"),
            "code-challenge", "plain", "clientId123456",
            new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        this.newTicketRegistry.addTicket(code);
        val ticket = this.newTicketRegistry.getTicket(code.getId(), OAuth20Code.class);
        assertNotNull(ticket);
    }

    @RepeatedTest(1)
    void verifyFailures() throws Throwable {
        val pool = mock(ObjectPool.class);
        val registry = new MemcachedTicketRegistry(CipherExecutor.noOp(), ticketSerializationManager, ticketCatalog, applicationContext, pool);
        assertNotNull(registry.updateTicket(new MockTicketGrantingTicket("casuser")));
        assertTrue(registry.deleteSingleTicket(new MockTicketGrantingTicket("casuser")) > 0);
        assertDoesNotThrow(() -> {
            val client = mock(MemcachedClientIF.class);
            when(pool.borrowObject()).thenReturn(client);
            when(client.set(anyString(), anyInt(), any())).thenThrow(new IllegalArgumentException());
            doThrow(new IllegalArgumentException()).when(pool).returnObject(any());
            registry.addTicket(new MockTicketGrantingTicket("casuser"));
        });
    }

    @TestConfiguration(value = "MemcachedTicketRegistryTestConfiguration", proxyBeanMethods = false)
    @Deprecated(since = "7.0.0")
    static class MemcachedTicketRegistryTestConfiguration implements ComponentSerializationPlanConfigurer {
        @Override
        public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
            plan.registerSerializableClass(MockTicketGrantingTicket.class);
            plan.registerSerializableClass(MockServiceTicket.class);
        }
    }
}

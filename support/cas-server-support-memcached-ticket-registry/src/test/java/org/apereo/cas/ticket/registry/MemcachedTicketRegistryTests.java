package org.apereo.cas.ticket.registry;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationComponentSerializationConfiguration;
import org.apereo.cas.config.CasCoreServicesComponentSerializationConfiguration;
import org.apereo.cas.config.CasCoreTicketComponentSerializationConfiguration;
import org.apereo.cas.config.CasCoreUtilSerializationConfiguration;
import org.apereo.cas.config.CasOAuth20ComponentSerializationConfiguration;
import org.apereo.cas.config.MemcachedTicketRegistryConfiguration;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.code.OAuth20DefaultOAuthCodeFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.util.serialization.ComponentSerializationPlan;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import lombok.Getter;
import lombok.val;
import net.spy.memcached.MemcachedClientIF;
import org.apache.commons.pool2.ObjectPool;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Lazy;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for MemcachedTicketRegistry class.
 *
 * @author Middleware Services
 * @since 3.0.0
 */
@SpringBootTest(classes = {
    MemcachedTicketRegistryConfiguration.class,
    CasCoreUtilSerializationConfiguration.class,
    CasCoreTicketComponentSerializationConfiguration.class,
    CasCoreAuthenticationComponentSerializationConfiguration.class,
    CasCoreServicesComponentSerializationConfiguration.class,
    CasOAuth20ComponentSerializationConfiguration.class,
    CasAuthenticationEventExecutionPlanTestConfiguration.class,
    MemcachedTicketRegistryTests.MemcachedTicketRegistryTestConfiguration.class,
    BaseTicketRegistryTests.SharedTestConfiguration.class
},
    properties = {
        "cas.ticket.registry.memcached.servers=localhost:11211",
        "cas.ticket.registry.memcached.failure-mode=Redistribute",
        "cas.ticket.registry.memcached.locator-type=ARRAY_MOD",
        "cas.ticket.registry.memcached.transcoder=KRYO",
        "cas.ticket.registry.memcached.hash-algorithm=FNV1A_64_HASH",
        "cas.ticket.registry.memcached.kryo-registration-required=true"
    })
@EnabledIfPortOpen(port = 11211)
@Tag("Memcached")
@Getter
public class MemcachedTicketRegistryTests extends BaseTicketRegistryTests {
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

    @Override
    protected boolean isIterableRegistry() {
        return false;
    }

    @RepeatedTest(2)
    public void verifyOAuthCodeIsAddedToMemcached() {
        val factory = new OAuth20DefaultOAuthCodeFactory(neverExpiresExpirationPolicyBuilder(), servicesManager);
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
    public void verifyFailures() {
        val pool = mock(ObjectPool.class);
        val registry = new MemcachedTicketRegistry(pool);
        assertNotNull(registry.updateTicket(new MockTicketGrantingTicket("casuser")));
        assertNotNull(registry.deleteSingleTicket(new MockTicketGrantingTicket("casuser").getId()));
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Exception {
                val client = mock(MemcachedClientIF.class);
                when(pool.borrowObject()).thenReturn(client);
                when(client.set(anyString(), anyInt(), any())).thenThrow(new IllegalArgumentException());
                doThrow(new IllegalArgumentException()).when(pool).returnObject(any());
                registry.addTicket(new MockTicketGrantingTicket("casuser"));
            }
        });
    }

    @TestConfiguration("MemcachedTicketRegistryTestConfiguration")
    @Lazy(false)
    public static class MemcachedTicketRegistryTestConfiguration implements ComponentSerializationPlanConfigurer {
        @Override
        public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
            plan.registerSerializableClass(MockTicketGrantingTicket.class);
            plan.registerSerializableClass(MockServiceTicket.class);
        }
    }
}

package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreTicketComponentSerializationConfiguration;
import org.apereo.cas.config.CasCoreUtilSerializationConfiguration;
import org.apereo.cas.config.CasOAuth20ComponentSerializationConfiguration;
import org.apereo.cas.config.MemcachedTicketRegistryConfiguration;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.code.OAuth20DefaultOAuthCodeFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.util.serialization.ComponentSerializationPlan;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Lazy;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

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
    CasOAuth20ComponentSerializationConfiguration.class,
    MemcachedTicketRegistryTests.MemcachedTicketRegistryTestConfiguration.class,
    BaseTicketRegistryTests.SharedTestConfiguration.class
},
    properties = {
        "cas.ticket.registry.memcached.servers=localhost:11211",
        "cas.ticket.registry.memcached.failure-mode=Redistribute",
        "cas.ticket.registry.memcached.locator-type=ARRAY_MOD",
        "cas.ticket.registry.memcached.hash-algorithm=FNV1A_64_HASH",
        "cas.ticket.registry.memcached.kryo-registration-required=true"
    })
@EnabledIfPortOpen(port = 11211)
@Tag("Memcached")
public class MemcachedTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry registry;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Override
    public TicketRegistry getNewTicketRegistry() {
        return registry;
    }

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
            new HashMap<>());
        this.registry.addTicket(code);
        val ticket = this.registry.getTicket(code.getId(), OAuth20Code.class);
        assertNotNull(ticket);
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

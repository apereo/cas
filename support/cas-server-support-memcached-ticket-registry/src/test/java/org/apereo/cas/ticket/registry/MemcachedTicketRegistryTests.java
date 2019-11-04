package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreUtilSerializationConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasOAuth20ComponentSerializationConfiguration;
import org.apereo.cas.config.MemcachedTicketRegistryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.code.OAuth20DefaultOAuthCodeFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.serialization.ComponentSerializationPlan;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

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
    CasOAuth20ComponentSerializationConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    MemcachedTicketRegistryTests.MemcachedTicketRegistryTestConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilSerializationConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class
},
    properties = {
        "cas.ticket.registry.memcached.servers=localhost:11211",
        "cas.ticket.registry.memcached.failureMode=Redistribute",
        "cas.ticket.registry.memcached.locatorType=ARRAY_MOD",
        "cas.ticket.registry.memcached.hashAlgorithm=FNV1A_64_HASH",
        "cas.ticket.registry.memcached.kryoRegistrationRequired=true"
    })
@EnabledIfContinuousIntegration
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
    public static class MemcachedTicketRegistryTestConfiguration implements ComponentSerializationPlanConfigurer {
        @Override
        public void configureComponentSerializationPlan(final ComponentSerializationPlan plan) {
            plan.registerSerializableClass(MockTicketGrantingTicket.class);
            plan.registerSerializableClass(MockServiceTicket.class);
        }
    }
}

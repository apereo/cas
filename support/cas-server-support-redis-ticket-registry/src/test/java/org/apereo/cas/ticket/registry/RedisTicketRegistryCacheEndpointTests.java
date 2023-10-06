package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.RedisCoreConfiguration;
import org.apereo.cas.config.RedisTicketRegistryConfiguration;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.TicketGrantingTicketIdGenerator;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.report.AbstractCasEndpointTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RedisTicketRegistryCacheEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnabledIfListeningOnPort(port = 6379)
@Tag("Redis")
@TestPropertySource(properties = "management.endpoint.redisTicketsCache.enabled=true")
@Tag("ActuatorEndpoint")
@Import({
    RedisCoreConfiguration.class,
    RedisTicketRegistryConfiguration.class
})
class RedisTicketRegistryCacheEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("redisTicketRegistryCacheEndpoint")
    private RedisTicketRegistryCacheEndpoint endpoint;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Test
    void verifyCachedTicket() throws Throwable {
        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        val generator = new TicketGrantingTicketIdGenerator(10, "redis");
        val ticket = new TicketGrantingTicketImpl(generator.getNewTicketId(TicketGrantingTicket.PREFIX),
            originalAuthn, NeverExpiresExpirationPolicy.INSTANCE);
        ticketRegistry.addTicket(ticket);
        var results = endpoint.fetchTicket(ticket.getId());
        assertTrue(results.getStatusCode().is2xxSuccessful());
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));

        endpoint.invalidateTicket(ticket.getId());
        results = endpoint.fetchTicket(ticket.getId());
        assertTrue(results.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND));
    }

    @Test
    void verifyUnknownTicket() throws Throwable {
        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        val generator = new TicketGrantingTicketIdGenerator(10, "redis");
        val ticket = new TicketGrantingTicketImpl(generator.getNewTicketId(TicketGrantingTicket.PREFIX),
            originalAuthn, NeverExpiresExpirationPolicy.INSTANCE);
        val results = endpoint.fetchTicket(ticket.getId());
        assertTrue(results.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND));
    }
    
    
}

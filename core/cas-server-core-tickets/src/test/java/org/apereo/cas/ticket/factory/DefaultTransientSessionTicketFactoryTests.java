package org.apereo.cas.ticket.factory;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultTransientSessionTicketFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = "cas.ticket.tst.time-to-kill-in-seconds=20")
@Tag("Tickets")
class DefaultTransientSessionTicketFactoryTests extends BaseTicketFactoryTests {
    @Test
    void verifyExpirationPolicy() throws Throwable {
        val factory = (TransientSessionTicketFactory) this.ticketFactory.get(TransientSessionTicket.class);
        val ticket = factory.create(RegisteredServiceTestUtils.getService("example"), new HashMap<>());
        assertNotNull(ticket);
        assertEquals(20, ticket.getExpirationPolicy().getTimeToLive());
    }

    @Test
    void verifyById() {
        val factory = (TransientSessionTicketFactory) this.ticketFactory.get(TransientSessionTicket.class);
        val ticket = factory.create(UUID.randomUUID().toString(), Map.of());
        assertNotNull(ticket);
        assertNull(ticket.getService());
    }

    @Test
    void verifyByServiceById() {
        val factory = (TransientSessionTicketFactory) this.ticketFactory.get(TransientSessionTicket.class);
        val ticket = factory.create(UUID.randomUUID().toString(),
            RegisteredServiceTestUtils.getService("example"), Map.of("key", "value"));
        assertNotNull(ticket);
        assertNotNull(ticket.getService());
    }

    @Test
    void verifyCustomExpirationPolicy() throws Throwable {
        val factory = (TransientSessionTicketFactory) this.ticketFactory.get(TransientSessionTicket.class);
        val ticket = factory.create(RegisteredServiceTestUtils.getService("example"),
            CollectionUtils.wrap(ExpirationPolicy.class.getName(),
                HardTimeoutExpirationPolicy.builder().timeToKillInSeconds(60).build()));
        assertNotNull(ticket);
        assertEquals(60, ticket.getExpirationPolicy().getTimeToLive());
    }
}

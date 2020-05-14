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

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultTransientSessionTicketFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = "cas.ticket.tst.timeToKillInSeconds=20")
@Tag("Simple")
public class DefaultTransientSessionTicketFactoryTests extends BaseTicketFactoryTests {
    @Test
    public void verifyExpirationPolicy() {
        val factory = (TransientSessionTicketFactory) this.ticketFactory.get(TransientSessionTicket.class);
        val ticket = factory.create(RegisteredServiceTestUtils.getService("example"), new HashMap<>(0));
        assertNotNull(ticket);
        assertEquals(20, ticket.getExpirationPolicy().getTimeToLive());
    }

    @Test
    public void verifyCustomExpirationPolicy() {
        val factory = (TransientSessionTicketFactory) this.ticketFactory.get(TransientSessionTicket.class);
        val ticket = factory.create(RegisteredServiceTestUtils.getService("example"),
            CollectionUtils.wrap(ExpirationPolicy.class.getName(),
                HardTimeoutExpirationPolicy.builder().timeToKillInSeconds(60).build()));
        assertNotNull(ticket);
        assertEquals(60, ticket.getExpirationPolicy().getTimeToLive());
    }
}

package org.apereo.cas.ticket.factory;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketFactory;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultServiceTicketFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Simple")
public class DefaultServiceTicketFactoryTests extends BaseTicketFactoryTests {

    @Test
    public void verifyCustomExpirationPolicy() {
        val factory = (ServiceTicketFactory) this.ticketFactory.get(ServiceTicket.class);
        val serviceTicket = factory.create(new MockTicketGrantingTicket("casuser"),
            RegisteredServiceTestUtils.getService("customExpirationPolicy"),
            true, ServiceTicket.class);
        assertNotNull(serviceTicket);
        assertEquals(666, serviceTicket.getExpirationPolicy().getTimeToLive());
    }

    @Test
    public void verifyDefaultExpirationPolicy() {
        val factory = (ServiceTicketFactory) this.ticketFactory.get(ServiceTicket.class);
        val serviceTicket = factory.create(new MockTicketGrantingTicket("casuser"),
            RegisteredServiceTestUtils.getService("defaultExpirationPolicy"),
            true, ServiceTicket.class);
        assertNotNull(serviceTicket);
        assertEquals(10, serviceTicket.getExpirationPolicy().getTimeToLive());
    }
}

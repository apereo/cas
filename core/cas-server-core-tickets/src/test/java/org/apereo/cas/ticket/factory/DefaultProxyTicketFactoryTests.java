package org.apereo.cas.ticket.factory;

import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.proxy.ProxyTicketFactory;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultProxyTicketFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Simple")
public class DefaultProxyTicketFactoryTests extends BaseTicketFactoryTests {
    @Test
    public void verifyCustomExpirationPolicy() {
        val tgt = new MockTicketGrantingTicket("casuser");
        val service = RegisteredServiceTestUtils.getService("customExpirationPolicy");
        val pgtFactory = (ProxyGrantingTicketFactory) this.ticketFactory.get(ProxyGrantingTicket.class);
        val pgt = pgtFactory.create(new MockServiceTicket("123456", service, tgt),
            RegisteredServiceTestUtils.getAuthentication(), ProxyGrantingTicket.class);
        val factory = (ProxyTicketFactory) this.ticketFactory.get(ProxyTicket.class);
        val ticket = factory.create(pgt, service, ProxyTicket.class);
        assertNotNull(ticket);
        assertEquals(1984, ticket.getExpirationPolicy().getTimeToLive());
    }

    @Test
    public void verifyDefaultExpirationPolicy() {
        val tgt = new MockTicketGrantingTicket("casuser");
        val service = RegisteredServiceTestUtils.getService("defaultExpirationPolicy");
        val pgtFactory = (ProxyGrantingTicketFactory) this.ticketFactory.get(ProxyGrantingTicket.class);
        val pgt = pgtFactory.create(new MockServiceTicket("123456", service, tgt),
            RegisteredServiceTestUtils.getAuthentication(), ProxyGrantingTicket.class);
        val factory = (ProxyTicketFactory) this.ticketFactory.get(ProxyTicket.class);
        val ticket = factory.create(pgt, service, ProxyTicket.class);
        assertNotNull(ticket);
        assertEquals(10, ticket.getExpirationPolicy().getTimeToLive());
    }
}

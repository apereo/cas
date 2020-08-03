package org.apereo.cas.ticket.factory;

import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceProxyGrantingTicketExpirationPolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultProxyGrantingTicketFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Tickets")
public class DefaultProxyGrantingTicketFactoryTests extends BaseTicketFactoryTests {

    @Test
    public void verifyMismatchedClass() {
        val tgt = new MockTicketGrantingTicket("casuser");
        val service = RegisteredServiceTestUtils.getService("otherExpirationPolicy");
        val pgtFactory = (ProxyGrantingTicketFactory) this.ticketFactory.get(ProxyGrantingTicket.class);
        val pgt = mock(ProxyGrantingTicket.class);
        assertThrows(ClassCastException.class, () ->
            pgtFactory.create(new MockServiceTicket("123456", service, tgt),
                RegisteredServiceTestUtils.getAuthentication(), pgt.getClass()));
    }

    @Test
    public void verifyCustomExpirationPolicy() {
        val defaultSvc = RegisteredServiceTestUtils.getRegisteredService("customPgtExpirationPolicy", RegexRegisteredService.class);
        defaultSvc.setProxyGrantingTicketExpirationPolicy(new DefaultRegisteredServiceProxyGrantingTicketExpirationPolicy(60));
        servicesManager.save(defaultSvc);

        val tgt = new MockTicketGrantingTicket("casuser");
        val service = RegisteredServiceTestUtils.getService("customPgtExpirationPolicy");
        val pgtFactory = (ProxyGrantingTicketFactory) this.ticketFactory.get(ProxyGrantingTicket.class);
        val pgt = pgtFactory.create(new MockServiceTicket("123456", service, tgt),
            RegisteredServiceTestUtils.getAuthentication(), ProxyGrantingTicket.class);
        assertNotNull(pgt);
        assertEquals(60, pgt.getExpirationPolicy().getTimeToLive());
    }

}

package org.apereo.cas.ticket.factory;

import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceProxyGrantingTicketExpirationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultProxyGrantingTicketFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Tickets")
class DefaultProxyGrantingTicketFactoryTests extends BaseTicketFactoryTests {
    

    @Test
    void verifyCustomExpirationPolicy() throws Throwable {
        val defaultSvc = RegisteredServiceTestUtils.getRegisteredService("customPgtExpirationPolicy", CasRegisteredService.class);
        defaultSvc.setProxyGrantingTicketExpirationPolicy(new DefaultRegisteredServiceProxyGrantingTicketExpirationPolicy(60));
        servicesManager.save(defaultSvc);

        val tgt = new MockTicketGrantingTicket("casuser");
        val service = RegisteredServiceTestUtils.getService("customPgtExpirationPolicy");
        val pgtFactory = (ProxyGrantingTicketFactory) this.ticketFactory.get(ProxyGrantingTicket.class);
        val pgt = pgtFactory.create(new MockServiceTicket("123456", service, tgt),
            RegisteredServiceTestUtils.getAuthentication());
        assertNotNull(pgt);
        assertEquals(60, pgt.getExpirationPolicy().getTimeToLive());
    }

}

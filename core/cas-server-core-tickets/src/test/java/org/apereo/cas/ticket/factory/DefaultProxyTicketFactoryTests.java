package org.apereo.cas.ticket.factory;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceProxyTicketExpirationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.proxy.ProxyTicketFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultProxyTicketFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Tickets")
@TestPropertySource(properties = "cas.ticket.crypto.enabled=true")
class DefaultProxyTicketFactoryTests extends BaseTicketFactoryTests {
    @Test
    void verifyCustomExpirationPolicy() throws Throwable {
        val svc = RegisteredServiceTestUtils.getRegisteredService("customExpirationPolicy", CasRegisteredService.class);
        svc.setProxyTicketExpirationPolicy(
            new DefaultRegisteredServiceProxyTicketExpirationPolicy(50, "1984"));
        servicesManager.save(svc);

        val tgt = new MockTicketGrantingTicket("casuser");
        val service = RegisteredServiceTestUtils.getService("customExpirationPolicy");
        val pgtFactory = (ProxyGrantingTicketFactory) ticketFactory.get(ProxyGrantingTicket.class);
        val pgt = pgtFactory.create(new MockServiceTicket("123456", service, tgt),
            RegisteredServiceTestUtils.getAuthentication());
        val factory = (ProxyTicketFactory) ticketFactory.get(ProxyTicket.class);
        val ticket = factory.create(pgt, service);
        assertNotNull(ticket);
        assertEquals(1984, ticket.getExpirationPolicy().getTimeToLive());
        
        assertTrue(ticketFactory.getExpirationPolicyBuilder().buildTicketExpirationPolicy().isExpired(ticket));
    }

    @Test
    void verifyDefaultExpirationPolicy() throws Throwable {
        val defaultSvc = RegisteredServiceTestUtils.getRegisteredService("defaultExpirationPolicy", CasRegisteredService.class);
        servicesManager.save(defaultSvc);
        val tgt = new MockTicketGrantingTicket("casuser");
        val service = RegisteredServiceTestUtils.getService("defaultExpirationPolicy");
        val pgtFactory = (ProxyGrantingTicketFactory) ticketFactory.get(ProxyGrantingTicket.class);
        val pgt = pgtFactory.create(new MockServiceTicket("123456", service, tgt),
            RegisteredServiceTestUtils.getAuthentication());
        val factory = (ProxyTicketFactory) ticketFactory.get(ProxyTicket.class);
        val ticket = factory.create(pgt, service);
        assertNotNull(ticket);
        assertEquals(10, ticket.getExpirationPolicy().getTimeToLive());
    }
    
    @Test
    void verifyDefaultTicketIdGenerator() throws Throwable {
        val defaultSvc = RegisteredServiceTestUtils.getRegisteredService("defaultExpirationPolicy", CasRegisteredService.class);
        servicesManager.save(defaultSvc);
        val tgt = new MockTicketGrantingTicket("casuser");
        val service = CoreAuthenticationTestUtils.getService("defaultExpirationPolicy");
        val pgtFactory = (ProxyGrantingTicketFactory) ticketFactory.get(ProxyGrantingTicket.class);
        val pgt = pgtFactory.create(new MockServiceTicket("123456", service, tgt),
            RegisteredServiceTestUtils.getAuthentication());
        val factory = (ProxyTicketFactory) ticketFactory.get(ProxyTicket.class);
        assertNotNull(factory.create(pgt, service));
    }
}

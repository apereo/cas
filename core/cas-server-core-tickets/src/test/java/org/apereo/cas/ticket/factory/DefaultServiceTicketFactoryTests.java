package org.apereo.cas.ticket.factory;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceServiceTicketExpirationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ProxyGrantingTicketIssuerTicket;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.proxy.ProxyTicket;
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
@Tag("Tickets")
class DefaultServiceTicketFactoryTests extends BaseTicketFactoryTests {

    @Test
    void verifyBadType() {
        val factory = (ServiceTicketFactory) this.ticketFactory.get(ServiceTicket.class);
        assertThrows(ClassCastException.class,
            () -> factory.create(new MockTicketGrantingTicket("casuser"),
                RegisteredServiceTestUtils.getService("customExpirationPolicy"),
                true, BaseMockTicketServiceTicket.class));
    }

    @Test
    void verifyCustomExpirationPolicy() throws Throwable {
        val svc = RegisteredServiceTestUtils.getRegisteredService("customExpirationPolicy", CasRegisteredService.class);
        svc.setServiceTicketExpirationPolicy(new DefaultRegisteredServiceServiceTicketExpirationPolicy(10, "666"));
        servicesManager.save(svc);

        val factory = (ServiceTicketFactory) this.ticketFactory.get(ServiceTicket.class);
        val serviceTicket = factory.create(new MockTicketGrantingTicket("casuser"),
            RegisteredServiceTestUtils.getService("customExpirationPolicy"),
            true, ServiceTicket.class);
        assertNotNull(serviceTicket);
        assertEquals(666, serviceTicket.getExpirationPolicy().getTimeToLive());
    }

    @Test
    void verifyDefaultExpirationPolicy() throws Throwable {
        val svc = RegisteredServiceTestUtils.getRegisteredService("defaultExpirationPolicy", CasRegisteredService.class);
        servicesManager.save(svc);

        val factory = (ServiceTicketFactory) this.ticketFactory.get(ServiceTicket.class);
        val serviceTicket = factory.create(new MockTicketGrantingTicket("casuser"),
            RegisteredServiceTestUtils.getService("defaultExpirationPolicy"),
            true, ServiceTicket.class);
        assertNotNull(serviceTicket);
        assertEquals(10, serviceTicket.getExpirationPolicy().getTimeToLive());
    }

    @Test
    void verifyCompactServiceTicketWithoutTgt() throws Throwable {
        val svc = RegisteredServiceTestUtils.getRegisteredService("defaultExpirationPolicy", CasRegisteredService.class);
        servicesManager.save(svc);
        val factory = (ServiceTicketFactory) ticketFactory.get(ServiceTicket.class);
        assertThrows(ClassCastException.class,
            () -> factory.create(RegisteredServiceTestUtils.getService("defaultExpirationPolicy"),
                CoreAuthenticationTestUtils.getAuthentication(), true, ProxyTicket.class));
        val serviceTicket = factory.create(RegisteredServiceTestUtils.getService("defaultExpirationPolicy"),
            CoreAuthenticationTestUtils.getAuthentication(), true, ServiceTicket.class);
        assertTrue(serviceTicket.isStateless());
        assertNotNull(serviceTicket.getAuthentication());
        val pgtIssuer = (ProxyGrantingTicketIssuerTicket) serviceTicket;
        val pgt = pgtIssuer.grantProxyGrantingTicket("PGT-123", CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE, proxyGrantingTicketTrackingPolicy);
        assertNotNull(pgt);
        assertNotNull(pgt.getAuthentication());
    }

    abstract static class BaseMockTicketServiceTicket implements TicketGrantingTicket {
        @Serial
        private static final long serialVersionUID = 6712185629825357896L;
    }
}

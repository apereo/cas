package org.apereo.cas.ticket.factory;

import module java.base;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceProxyGrantingTicketExpirationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultProxyGrantingTicketFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Tickets")
class DefaultProxyGrantingTicketFactoryTests {

    @Nested
    class AllPGTsTrackingPolicy extends BaseTicketFactoryTests {

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

        @Test
        void verifyProxyGrantingTicketTrackingPolicy() throws Throwable {
            val service = RegisteredServiceTestUtils.getService("PGT_ALL_TRACK_POLICY");

            val tgt = new MockTicketGrantingTicket("casuser");
            val pgtFactory = (ProxyGrantingTicketFactory) this.ticketFactory.get(ProxyGrantingTicket.class);

            val st1 = tgt.grantServiceTicket("ST-1", service, null, false, serviceTicketSessionTrackingPolicy);
            val pgt1 = pgtFactory.create(st1, RegisteredServiceTestUtils.getAuthentication());
            assertNotNull(pgt1);
            val st2 = tgt.grantServiceTicket("ST-2", service, null, false, serviceTicketSessionTrackingPolicy);
            val pgt2 = pgtFactory.create(st2, RegisteredServiceTestUtils.getAuthentication());
            assertNotNull(pgt2);
            assertEquals(2, tgt.getProxyGrantingTickets().size());
        }

    }

    @Nested
    @TestPropertySource(properties = "cas.ticket.st.proxy-granting-ticket-tracking-policy=MOST_RECENT")
    class MostRecentPGTTrackingPolicy extends BaseTicketFactoryTests {
        
        @Test
        void verifyProxyGrantingTicketTrackingPolicy() throws Throwable {
            val service = RegisteredServiceTestUtils.getService("PGT_MOST_RECENT_TRACK_POLICY");

            val tgt = new MockTicketGrantingTicket("casuser");
            val pgtFactory = (ProxyGrantingTicketFactory) this.ticketFactory.get(ProxyGrantingTicket.class);

            val st1 = tgt.grantServiceTicket("ST-1", service, null, false, serviceTicketSessionTrackingPolicy);
            val pgt1 = pgtFactory.create(st1, RegisteredServiceTestUtils.getAuthentication());
            assertNotNull(pgt1);
            val st2 = tgt.grantServiceTicket("ST-2", service, null, false, serviceTicketSessionTrackingPolicy);
            val pgt2 = pgtFactory.create(st2, RegisteredServiceTestUtils.getAuthentication());
            assertNotNull(pgt2);
            assertEquals(1, tgt.getProxyGrantingTickets().size());
        }

    }
}

package org.jasig.cas.ticket;

import static org.junit.Assert.*;

import org.jasig.cas.util.ServicesTestUtils;
import org.jasig.cas.util.AuthTestUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.junit.Test;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class ServiceTicketImplTests {

    private final TicketGrantingTicketImpl ticketGrantingTicket = new TicketGrantingTicketImpl("test",
            AuthTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

    private final UniqueTicketIdGenerator uniqueTicketIdGenerator = new DefaultUniqueTicketIdGenerator();

    @Test(expected = Exception.class)
    public void verifyNoService() {
        new ServiceTicketImpl("stest1", this.ticketGrantingTicket, null, true, new NeverExpiresExpirationPolicy());
    }

    @Test(expected = Exception.class)
    public void verifyNoTicket() {
        new ServiceTicketImpl("stest1", null, ServicesTestUtils.getService(), true, new NeverExpiresExpirationPolicy());
    }

    @Test
    public void verifyIsFromNewLoginTrue() {
        final ServiceTicket s = new ServiceTicketImpl("stest1", this.ticketGrantingTicket,
                ServicesTestUtils.getService(), true,
                new NeverExpiresExpirationPolicy());
        assertTrue(s.isFromNewLogin());
    }

    @Test
    public void verifyIsFromNewLoginFalse() {
        final ServiceTicket s = new ServiceTicketImpl("stest1", this.ticketGrantingTicket,
                ServicesTestUtils.getService(), false,
                new NeverExpiresExpirationPolicy());
        assertFalse(s.isFromNewLogin());
    }

    @Test
    public void verifyGetService() {
        final Service simpleService = ServicesTestUtils.getService();
        final ServiceTicket s = new ServiceTicketImpl("stest1", this.ticketGrantingTicket, simpleService, false,
                new NeverExpiresExpirationPolicy());
        assertEquals(simpleService, s.getService());
    }

    @Test
    public void verifyGetTicket() {
        final Service simpleService = ServicesTestUtils.getService();
        final ServiceTicket s = new ServiceTicketImpl("stest1", this.ticketGrantingTicket, simpleService, false,
                new NeverExpiresExpirationPolicy());
        assertEquals(this.ticketGrantingTicket, s.getGrantingTicket());
    }

    @Test
    public void verifyIsExpiredTrueBecauseOfRoot() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test",
                AuthTestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        final ServiceTicket s = t.grantServiceTicket(this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                ServicesTestUtils.getService(), new NeverExpiresExpirationPolicy(), false, true);
        t.markTicketExpired();

        assertTrue(s.isExpired());
    }

    @Test
    public void verifyIsExpiredFalse() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test",
                AuthTestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        final ServiceTicket s = t.grantServiceTicket(this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                ServicesTestUtils.getService(),
                new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000), false, true);
        assertFalse(s.isExpired());
    }

    @Test
    public void verifyTicketGrantingTicket() {
        final Authentication a = AuthTestUtils.getAuthentication();
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test",
                AuthTestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        final ServiceTicket s = t.grantServiceTicket(this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                ServicesTestUtils.getService(),
                new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000), false, true);
        final TicketGrantingTicket t1 = s.grantProxyGrantingTicket(
                this.uniqueTicketIdGenerator.getNewTicketId(TicketGrantingTicket.PREFIX), a,
                new NeverExpiresExpirationPolicy());

        assertEquals(a, t1.getAuthentication());
    }

    @Test
    public void verifyTicketGrantingTicketGrantedTwice() {
        final Authentication a = AuthTestUtils.getAuthentication();
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test",
                AuthTestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        final ServiceTicket s = t.grantServiceTicket(this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                ServicesTestUtils.getService(),
                new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000), false, true);
        s.grantProxyGrantingTicket(this.uniqueTicketIdGenerator.getNewTicketId(TicketGrantingTicket.PREFIX), a,
                new NeverExpiresExpirationPolicy());

        try {
            s.grantProxyGrantingTicket(this.uniqueTicketIdGenerator.getNewTicketId(TicketGrantingTicket.PREFIX), a,
                    new NeverExpiresExpirationPolicy());
            fail("Exception expected.");
        } catch (final Exception e) {
            return;
        }
    }
}

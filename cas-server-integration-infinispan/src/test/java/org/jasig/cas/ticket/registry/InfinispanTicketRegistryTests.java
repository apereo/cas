package org.jasig.cas.ticket.registry;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.TestUtils;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.ServiceTicket;

/**
 * This is {@link InfinispanTicketRegistryTests}.
 *
 * @since 4.2.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/infinispan-springcache-tests.xml")
public class InfinispanTicketRegistryTests {

    @Autowired
    @Qualifier("infinispanTicketRegistry")
    private TicketRegistry infinispanTicketRegistry;

    @Test
    public void updateTicketShouldOverwriteTicketInStorage() {
        final Ticket ticket = getTicket();
        infinispanTicketRegistry.addTicket(ticket);
        assertFalse(infinispanTicketRegistry.getTicket(ticket.getId()).isExpired());
        final TicketGrantingTicket ticket2 = (TicketGrantingTicket) ticket;
        ticket2.markTicketExpired();
        infinispanTicketRegistry.addTicket(ticket);
        assertTrue(infinispanTicketRegistry.getTicket(ticket.getId()).isExpired());
    }

    @Test
    public void addTicketExistsInCache() {
        final Ticket ticket = getTicket();
        infinispanTicketRegistry.addTicket(ticket);
        assertEquals(infinispanTicketRegistry.getTicket(ticket.getId()), ticket);
    }

    @Test
    public void deleteTicketRemovesFromCacheReturnsTrue() {
        final Ticket ticket = getTicket();
        infinispanTicketRegistry.addTicket(ticket);
        assertTrue(infinispanTicketRegistry.deleteTicket(ticket.getId()) == 1);
        assertNull(infinispanTicketRegistry.getTicket(ticket.getId()));
    }

    @Test
    public void deleteTicketOnNonExistingTicketReturnsFalse() {
        final String ticketId = "does_not_exist";
        assertFalse(infinispanTicketRegistry.deleteTicket(ticketId) == 1);
    }

    @Test
    public void getTicketReturnsTicketFromCacheOrNull() {
        final Ticket ticket = getTicket();
        infinispanTicketRegistry.addTicket(ticket);
        assertEquals(infinispanTicketRegistry.getTicket(ticket.getId()), ticket);
        assertNull(infinispanTicketRegistry.getTicket(""));
    }

    private Ticket getTicket() {
        final Authentication authentication = TestUtils.getAuthentication();
        return new TicketGrantingTicketImpl("123", authentication, new NeverExpiresExpirationPolicy());
    }

    @Test
    public void verifyDeleteTicketWithPGT() {
        final Authentication a = TestUtils.getAuthentication();
        this.infinispanTicketRegistry.addTicket(new TicketGrantingTicketImpl(
                "TGT", a, new NeverExpiresExpirationPolicy()));
        final TicketGrantingTicket tgt = this.infinispanTicketRegistry.getTicket(
                "TGT", TicketGrantingTicket.class);

        final Service service = org.jasig.cas.services.TestUtils.getService("TGT_DELETE_TEST");

        final ServiceTicket st1 = tgt.grantServiceTicket(
                "ST1", service, new NeverExpiresExpirationPolicy(), true, false);

        this.infinispanTicketRegistry.addTicket(st1);

        assertNotNull(this.infinispanTicketRegistry.getTicket("TGT", TicketGrantingTicket.class));
        assertNotNull(this.infinispanTicketRegistry.getTicket("ST1", ServiceTicket.class));
        final TicketGrantingTicket pgt = st1.grantProxyGrantingTicket("PGT-1", a, new NeverExpiresExpirationPolicy());
        assertEquals(a, pgt.getAuthentication());

        this.infinispanTicketRegistry.addTicket(pgt);
        assertTrue("TGT and children were deleted", this.infinispanTicketRegistry.deleteTicket(tgt.getId()) == 3);

        assertNull(this.infinispanTicketRegistry.getTicket("TGT", TicketGrantingTicket.class));
        assertNull(this.infinispanTicketRegistry.getTicket("ST1", ServiceTicket.class));
        assertNull(this.infinispanTicketRegistry.getTicket("PGT-1", ServiceTicket.class));
    }
}

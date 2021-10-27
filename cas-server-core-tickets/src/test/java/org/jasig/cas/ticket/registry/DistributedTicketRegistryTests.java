package org.jasig.cas.ticket.registry;

import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.TestUtils;
import org.jasig.cas.authentication.principal.Service;

/**
 *
 * @author Scott Battaglia
 * @since 3.1
 *
 */
public final class DistributedTicketRegistryTests {

    private TestDistributedTicketRegistry ticketRegistry;

    private boolean wasTicketUpdated;

    public void setWasTicketUpdated(final boolean wasTicketUpdated) {
        this.wasTicketUpdated = wasTicketUpdated;
    }

    @Before
    public void setUp() throws Exception {
        this.ticketRegistry = new TestDistributedTicketRegistry(this);
        this.wasTicketUpdated = false;
    }

    @Test
    public void verifyProxiedInstancesEqual() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test",
                org.jasig.cas.authentication.TestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        this.ticketRegistry.addTicket(t);

        final TicketGrantingTicket returned = (TicketGrantingTicket) this.ticketRegistry.getTicket("test");
        assertEquals(t, returned);
        assertEquals(returned, t);

        assertEquals(t.getCreationTime(), returned.getCreationTime());
        assertEquals(t.getAuthentication(), returned.getAuthentication());
        assertEquals(t.getCountOfUses(), returned.getCountOfUses());
        assertEquals(t.getGrantingTicket(), returned.getGrantingTicket());
        assertEquals(t.getId(), returned.getId());
        assertEquals(t.getChainedAuthentications(), returned.getChainedAuthentications());
        assertEquals(t.isExpired(), returned.isExpired());
        assertEquals(t.isRoot(), returned.isRoot());

        final ServiceTicket s = t.grantServiceTicket("stest", org.jasig.cas.services.TestUtils.getService(),
                new NeverExpiresExpirationPolicy(), false, true);
        this.ticketRegistry.addTicket(s);

        final ServiceTicket sreturned = (ServiceTicket) this.ticketRegistry.getTicket("stest");
        assertEquals(s, sreturned);
        assertEquals(sreturned, s);

        assertEquals(s.getCreationTime(), sreturned.getCreationTime());
        assertEquals(s.getCountOfUses(), sreturned.getCountOfUses());
        assertEquals(s.getGrantingTicket(), sreturned.getGrantingTicket());
        assertEquals(s.getId(), sreturned.getId());
        assertEquals(s.isExpired(), sreturned.isExpired());
        assertEquals(s.getService(), sreturned.getService());
        assertEquals(s.isFromNewLogin(), sreturned.isFromNewLogin());
    }

    @Test
    public void verifyUpdateOfRegistry() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test",
                org.jasig.cas.authentication.TestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        this.ticketRegistry.addTicket(t);
        final TicketGrantingTicket returned = (TicketGrantingTicket) this.ticketRegistry.getTicket("test");

        final ServiceTicket s = returned.grantServiceTicket("test2", org.jasig.cas.services.TestUtils.getService(),
                new NeverExpiresExpirationPolicy(), true, true);

        this.ticketRegistry.addTicket(s);
        final ServiceTicket s2 = (ServiceTicket) this.ticketRegistry.getTicket("test2");
        assertNotNull(s2.grantProxyGrantingTicket("ff", org.jasig.cas.authentication.TestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy()));

        assertTrue(s2.isValidFor(org.jasig.cas.services.TestUtils.getService()));
        assertTrue(this.wasTicketUpdated);

        returned.markTicketExpired();
        assertTrue(t.isExpired());
    }

    @Test
    public void verifyTicketDoesntExist() {
        assertNull(this.ticketRegistry.getTicket("fdfas"));
    }

    @Test
    public void verifyDeleteTicketWithPGT() {
       final Authentication a = TestUtils.getAuthentication();
        this.ticketRegistry.addTicket(new TicketGrantingTicketImpl(
                "TGT", a, new NeverExpiresExpirationPolicy()));
        final TicketGrantingTicket tgt = this.ticketRegistry.getTicket(
                "TGT", TicketGrantingTicket.class);

        final Service service = TestUtils.getService("TGT_DELETE_TEST");

        final ServiceTicket st1 = tgt.grantServiceTicket(
                "ST1", service, new NeverExpiresExpirationPolicy(), true, false);

        this.ticketRegistry.addTicket(st1);

        assertNotNull(this.ticketRegistry.getTicket("TGT", TicketGrantingTicket.class));
        assertNotNull(this.ticketRegistry.getTicket("ST1", ServiceTicket.class));

        final TicketGrantingTicket pgt = st1.grantProxyGrantingTicket("PGT-1", a, new NeverExpiresExpirationPolicy());
        assertEquals(a, pgt.getAuthentication());

        this.ticketRegistry.addTicket(pgt);
        assertTrue("TGT and children were deleted", this.ticketRegistry.deleteTicket(tgt.getId()) == 3);

        assertNull(this.ticketRegistry.getTicket("TGT", TicketGrantingTicket.class));
        assertNull(this.ticketRegistry.getTicket("ST1", ServiceTicket.class));
        assertNull(this.ticketRegistry.getTicket("PGT-1", ServiceTicket.class));
    }

    private static class TestDistributedTicketRegistry extends AbstractDistributedTicketRegistry {
        private final DistributedTicketRegistryTests parent;
        private final Map<String, Ticket> tickets = new HashMap<>();

        TestDistributedTicketRegistry(final DistributedTicketRegistryTests parent) {
            this.parent = parent;
        }

        @Override
        protected void updateTicket(final Ticket ticket) {
            this.parent.setWasTicketUpdated(true);
        }

        @Override
        public void addTicket(final Ticket ticket) {
            this.tickets.put(ticket.getId(), ticket);
        }

        @Override
        public Ticket getTicket(final String ticketId) {
            return getProxiedTicketInstance(this.tickets.get(ticketId));
        }

        @Override
        public Collection<Ticket> getTickets() {
            return this.tickets.values();
        }

        @Override
        protected boolean needsCallback() {
            return true;
        }

        @Override
        public boolean deleteSingleTicket(final String ticketId) {
            return this.tickets.remove(ticketId) != null;
        }
    }
}

package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.TestUtils;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class DistributedTicketRegistryTests {
    private static final String TGT_NAME = "TGT";

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
                TestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        this.ticketRegistry.addTicket(t);

        final TicketGrantingTicket returned = (TicketGrantingTicket) this.ticketRegistry.getTicket("test");
        assertEquals(t, returned);
        assertEquals(returned, t);

        assertEquals(t.getCreationTime(), returned.getCreationTime());
        Assert.assertEquals(t.getAuthentication(), returned.getAuthentication());
        assertEquals(t.getCountOfUses(), returned.getCountOfUses());
        assertEquals(t.getGrantingTicket(), returned.getGrantingTicket());
        assertEquals(t.getId(), returned.getId());
        assertEquals(t.getChainedAuthentications(), returned.getChainedAuthentications());
        assertEquals(t.isExpired(), returned.isExpired());
        assertEquals(t.isRoot(), returned.isRoot());

        final ServiceTicket s = t.grantServiceTicket("stest", org.apereo.cas.services.TestUtils.getService(),
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
        Assert.assertEquals(s.getService(), sreturned.getService());
        assertEquals(s.isFromNewLogin(), sreturned.isFromNewLogin());
    }

    @Test
    public void verifyUpdateOfRegistry() throws AbstractTicketException {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test",
                TestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        this.ticketRegistry.addTicket(t);
        final TicketGrantingTicket returned = (TicketGrantingTicket) this.ticketRegistry.getTicket("test");

        final ServiceTicket s = returned.grantServiceTicket("test2", org.apereo.cas.services.TestUtils.getService(),
                new NeverExpiresExpirationPolicy(), false, true);

        this.ticketRegistry.addTicket(s);
        final ServiceTicket s2 = (ServiceTicket) this.ticketRegistry.getTicket("test2");
        assertNotNull(s2.grantProxyGrantingTicket("ff", TestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy()));

        assertTrue(s2.isValidFor(org.apereo.cas.services.TestUtils.getService()));
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
                TGT_NAME, a, new NeverExpiresExpirationPolicy()));
        final TicketGrantingTicket tgt = this.ticketRegistry.getTicket(
                TGT_NAME, TicketGrantingTicket.class);

        final Service service = TestUtils.getService("TGT_DELETE_TEST");

        final ServiceTicket st1 = tgt.grantServiceTicket(
                "ST1", service, new NeverExpiresExpirationPolicy(), true, true);

        this.ticketRegistry.addTicket(st1);

        assertNotNull(this.ticketRegistry.getTicket(TGT_NAME, TicketGrantingTicket.class));
        assertNotNull(this.ticketRegistry.getTicket("ST1", ServiceTicket.class));

        final ProxyGrantingTicket pgt = st1.grantProxyGrantingTicket("PGT-1", a, new NeverExpiresExpirationPolicy());
        assertEquals(a, pgt.getAuthentication());

        this.ticketRegistry.addTicket(pgt);
        assertSame(3, this.ticketRegistry.deleteTicket(tgt.getId()));

        assertNull(this.ticketRegistry.getTicket(TGT_NAME, TicketGrantingTicket.class));
        assertNull(this.ticketRegistry.getTicket("ST1", ServiceTicket.class));
        assertNull(this.ticketRegistry.getTicket("PGT-1", ProxyGrantingTicket.class));
    }

    private static class TestDistributedTicketRegistry extends AbstractTicketRegistry {
        private DistributedTicketRegistryTests parent;
        private Map<String, Ticket> tickets = new HashMap<>();

        TestDistributedTicketRegistry(final DistributedTicketRegistryTests parent) {
            this.parent = parent;
        }

        @Override
        public void updateTicket(final Ticket ticket) {
            this.parent.setWasTicketUpdated(true);
        }

        @Override
        public void addTicket(final Ticket ticket) {
            this.tickets.put(ticket.getId(), ticket);
            updateTicket(ticket);
        }

        @Override
        public Ticket getTicket(final String ticketId) {
            return this.tickets.get(ticketId);
        }

        @Override
        public Collection<Ticket> getTickets() {
            return this.tickets.values();
        }
        
        @Override
        public boolean deleteSingleTicket(final String ticketId) {
            return this.tickets.remove(ticketId) != null;
        }
    }
}

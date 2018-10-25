package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;

import lombok.Setter;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
@Setter
public class DistributedTicketRegistryTests {

    private static final String TGT_NAME = "TGT";

    private static final String TGT_ID = "test";

    private TestDistributedTicketRegistry ticketRegistry;

    private boolean wasTicketUpdated;

    @BeforeEach
    public void initialize() {
        this.ticketRegistry = new TestDistributedTicketRegistry(this);
        this.wasTicketUpdated = false;
    }

    @Test
    public void verifyProxiedInstancesEqual() {
        val t = new TicketGrantingTicketImpl(TGT_ID, CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        this.ticketRegistry.addTicket(t);
        val returned = (TicketGrantingTicket) this.ticketRegistry.getTicket(TGT_ID);
        assertEquals(t, returned);
        assertEquals(returned, t);
        assertEquals(t.getCreationTime(), returned.getCreationTime());
        assertEquals(t.getAuthentication(), returned.getAuthentication());
        assertEquals(t.getCountOfUses(), returned.getCountOfUses());
        assertEquals(t.getTicketGrantingTicket(), returned.getTicketGrantingTicket());
        assertEquals(t.getId(), returned.getId());
        assertEquals(t.getChainedAuthentications(), returned.getChainedAuthentications());
        assertEquals(t.isExpired(), returned.isExpired());
        assertEquals(t.isRoot(), returned.isRoot());
        val s = t.grantServiceTicket("stest", RegisteredServiceTestUtils.getService(), new NeverExpiresExpirationPolicy(), false, true);
        this.ticketRegistry.addTicket(s);
        val sreturned = (ServiceTicket) this.ticketRegistry.getTicket("stest");
        assertEquals(s, sreturned);
        assertEquals(sreturned, s);
        assertEquals(s.getCreationTime(), sreturned.getCreationTime());
        assertEquals(s.getCountOfUses(), sreturned.getCountOfUses());
        assertEquals(s.getTicketGrantingTicket(), sreturned.getTicketGrantingTicket());
        assertEquals(s.getId(), sreturned.getId());
        assertEquals(s.isExpired(), sreturned.isExpired());
        assertEquals(s.getService(), sreturned.getService());
        assertEquals(s.isFromNewLogin(), sreturned.isFromNewLogin());
    }

    @Test
    public void verifyUpdateOfRegistry() throws AbstractTicketException {
        val t = new TicketGrantingTicketImpl(TGT_ID, CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        this.ticketRegistry.addTicket(t);
        val returned = (TicketGrantingTicket) this.ticketRegistry.getTicket(TGT_ID);
        val s = returned.grantServiceTicket("test2", RegisteredServiceTestUtils.getService(), new NeverExpiresExpirationPolicy(), false, true);
        this.ticketRegistry.addTicket(s);
        val s2 = (ServiceTicket) this.ticketRegistry.getTicket("test2");
        assertNotNull(s2.grantProxyGrantingTicket("ff", CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy()));
        assertTrue(s2.isValidFor(RegisteredServiceTestUtils.getService()));
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
        val a = CoreAuthenticationTestUtils.getAuthentication();
        this.ticketRegistry.addTicket(new TicketGrantingTicketImpl(TGT_NAME, a, new NeverExpiresExpirationPolicy()));
        val tgt = this.ticketRegistry.getTicket(TGT_NAME, TicketGrantingTicket.class);
        val service = CoreAuthenticationTestUtils.getService("TGT_DELETE_TEST");
        val st1 = tgt.grantServiceTicket("ST1", service, new NeverExpiresExpirationPolicy(), true, true);
        this.ticketRegistry.addTicket(st1);
        assertNotNull(this.ticketRegistry.getTicket(TGT_NAME, TicketGrantingTicket.class));
        assertNotNull(this.ticketRegistry.getTicket("ST1", ServiceTicket.class));
        val pgt = st1.grantProxyGrantingTicket("PGT-1", a, new NeverExpiresExpirationPolicy());
        assertEquals(a, pgt.getAuthentication());
        this.ticketRegistry.addTicket(pgt);
        assertSame(3, this.ticketRegistry.deleteTicket(tgt.getId()));
        assertNull(this.ticketRegistry.getTicket(TGT_NAME, TicketGrantingTicket.class));
        assertNull(this.ticketRegistry.getTicket("ST1", ServiceTicket.class));
        assertNull(this.ticketRegistry.getTicket("PGT-1", ProxyGrantingTicket.class));
    }

    private static class TestDistributedTicketRegistry extends AbstractTicketRegistry {

        private final DistributedTicketRegistryTests parent;

        private final Map<String, Ticket> tickets = new HashMap<>();

        TestDistributedTicketRegistry(final DistributedTicketRegistryTests parent) {
            this.parent = parent;
        }

        @Override
        public Ticket updateTicket(final Ticket ticket) {
            this.parent.setWasTicketUpdated(true);
            return ticket;
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
        public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
            return getTicket(ticketId);
        }

        @Override
        public Collection<Ticket> getTickets() {
            return this.tickets.values();
        }

        @Override
        public boolean deleteSingleTicket(final String ticketId) {
            return this.tickets.remove(ticketId) != null;
        }

        @Override
        public long deleteAll() {
            val size = this.tickets.size();
            this.tickets.clear();
            return size;
        }
    }
}

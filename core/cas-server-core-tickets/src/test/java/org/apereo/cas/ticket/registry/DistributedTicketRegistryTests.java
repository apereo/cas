package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ProxyGrantingTicketIssuerTicket;
import org.apereo.cas.ticket.RenewableServiceTicket;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketSessionTrackingPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

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
@Tag("Tickets")
@SpringBootTest(classes = BaseTicketRegistryTests.SharedTestConfiguration.class)
public class DistributedTicketRegistryTests {

    private static final String TGT_NAME = "TGT";

    private static final String TGT_ID = "test";

    private TestDistributedTicketRegistry ticketRegistry;

    private boolean wasTicketUpdated;

    @Autowired
    @Qualifier(ServiceTicketSessionTrackingPolicy.BEAN_NAME)
    private ServiceTicketSessionTrackingPolicy serviceTicketSessionTrackingPolicy;

    
    @BeforeEach
    public void initialize() {
        ticketRegistry = new TestDistributedTicketRegistry(this);
        wasTicketUpdated = false;
    }

    @Test
    public void verifyProxiedInstancesEqual() throws Exception {
        val t = new TicketGrantingTicketImpl(TGT_ID, CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE);
        ticketRegistry.addTicket(t);
        val returned = (TicketGrantingTicket) ticketRegistry.getTicket(TGT_ID);
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
        val s = t.grantServiceTicket("stest", RegisteredServiceTestUtils.getService(),
            NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy);
        ticketRegistry.addTicket(s);
        val sreturned = (ServiceTicket) ticketRegistry.getTicket("stest");
        assertEquals(s, sreturned);
        assertEquals(sreturned, s);
        assertEquals(s.getCreationTime(), sreturned.getCreationTime());
        assertEquals(s.getCountOfUses(), sreturned.getCountOfUses());
        assertEquals(s.getTicketGrantingTicket(), sreturned.getTicketGrantingTicket());
        assertEquals(s.getId(), sreturned.getId());
        assertEquals(s.isExpired(), sreturned.isExpired());
        assertEquals(s.getService(), sreturned.getService());
        assertEquals(((RenewableServiceTicket) s).isFromNewLogin(), ((RenewableServiceTicket) sreturned).isFromNewLogin());
    }

    @Test
    public void verifyUpdateOfRegistry() throws Exception {
        val t = new TicketGrantingTicketImpl(TGT_ID, CoreAuthenticationTestUtils.getAuthentication(), 
            NeverExpiresExpirationPolicy.INSTANCE);
        ticketRegistry.addTicket(t);
        val returned = (TicketGrantingTicket) ticketRegistry.getTicket(TGT_ID);
        val s = (ProxyGrantingTicketIssuerTicket) returned.grantServiceTicket("test2", 
            RegisteredServiceTestUtils.getService(), NeverExpiresExpirationPolicy.INSTANCE,
            false, this.serviceTicketSessionTrackingPolicy);
        ticketRegistry.addTicket(s);
        val s2 = (ProxyGrantingTicketIssuerTicket) ticketRegistry.getTicket("test2");
        assertNotNull(s2.grantProxyGrantingTicket("ff", CoreAuthenticationTestUtils.getAuthentication(), 
            NeverExpiresExpirationPolicy.INSTANCE));
        assertTrue(wasTicketUpdated);
        returned.markTicketExpired();
        assertTrue(t.isExpired());
    }

    @Test
    public void verifyTicketDoesntExist() {
        assertNull(ticketRegistry.getTicket("fdfas"));
    }

    @Test
    public void verifyDeleteTicketWithPGT() throws Exception {
        val a = CoreAuthenticationTestUtils.getAuthentication();
        ticketRegistry.addTicket(new TicketGrantingTicketImpl(TGT_NAME, a, NeverExpiresExpirationPolicy.INSTANCE));
        val tgt = ticketRegistry.getTicket(TGT_NAME, TicketGrantingTicket.class);
        val service = CoreAuthenticationTestUtils.getService("TGT_DELETE_TEST");
        val st1 = (ProxyGrantingTicketIssuerTicket) tgt.grantServiceTicket("ST1", 
            service, NeverExpiresExpirationPolicy.INSTANCE, true, serviceTicketSessionTrackingPolicy);
        ticketRegistry.addTicket(st1);
        assertNotNull(ticketRegistry.getTicket(TGT_NAME, TicketGrantingTicket.class));
        assertNotNull(ticketRegistry.getTicket("ST1", ServiceTicket.class));
        val pgt = st1.grantProxyGrantingTicket("PGT-1", a, NeverExpiresExpirationPolicy.INSTANCE);
        assertEquals(a, pgt.getAuthentication());
        ticketRegistry.addTicket(pgt);
        assertSame(3, ticketRegistry.deleteTicket(tgt.getId()));
        assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket(TGT_NAME, TicketGrantingTicket.class));
        assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket("ST1", ServiceTicket.class));
        assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket("PGT-1", ProxyGrantingTicket.class));
    }

    @RequiredArgsConstructor
    private static class TestDistributedTicketRegistry extends AbstractTicketRegistry {

        private final DistributedTicketRegistryTests parent;

        private final Map<String, Ticket> tickets = new HashMap<>();

        @Override
        public Ticket updateTicket(final Ticket ticket) {
            parent.setWasTicketUpdated(true);
            return ticket;
        }

        @Override
        public void addTicketInternal(final Ticket ticket) {
            tickets.put(ticket.getId(), ticket);
            updateTicket(ticket);
        }

        @Override
        public Ticket getTicket(final String ticketId) {
            return tickets.get(ticketId);
        }

        @Override
        public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
            return getTicket(ticketId);
        }

        @Override
        public Collection<Ticket> getTickets() {
            return tickets.values();
        }

        @Override
        public long deleteSingleTicket(final String ticketId) {
            return tickets.remove(ticketId) != null ? 1 : 0;
        }

        @Override
        public long deleteAll() {
            val size = tickets.size();
            tickets.clear();
            return size;
        }

        @Override
        public long countSessionsFor(final String principalId) {
            return 0;
        }
    }
}

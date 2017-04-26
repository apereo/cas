package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.TestUtils;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.registry.config.InfinispanTicketRegistryConfiguration;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.apereo.cas.authentication.principal.Service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * This is {@link InfinispanTicketRegistryTests}.
 *
 * @since 4.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {RefreshAutoConfiguration.class, InfinispanTicketRegistryConfiguration.class})
public class InfinispanTicketRegistryTests {
    private static final String TGT_NAME = "TGT";

    @Autowired
    @Qualifier("ticketRegistry")
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
        Assert.assertEquals(infinispanTicketRegistry.getTicket(ticket.getId()), ticket);
    }

    @Test
    public void deleteTicketRemovesFromCacheReturnsTrue() {
        final Ticket ticket = getTicket();
        infinispanTicketRegistry.addTicket(ticket);
        assertSame(1, infinispanTicketRegistry.deleteTicket(ticket.getId()));
        assertNull(infinispanTicketRegistry.getTicket(ticket.getId()));
    }

    @Test
    public void deleteTicketOnNonExistingTicketReturnsFalse() {
        final String ticketId = "does_not_exist";
        assertSame(0, infinispanTicketRegistry.deleteTicket(ticketId));
    }

    @Test
    public void getTicketReturnsTicketFromCacheOrNull() {
        final Ticket ticket = getTicket();
        infinispanTicketRegistry.addTicket(ticket);
        Assert.assertEquals(infinispanTicketRegistry.getTicket(ticket.getId()), ticket);
        assertNull(infinispanTicketRegistry.getTicket(""));
    }

    private static Ticket getTicket() {
        final Authentication authentication = TestUtils.getAuthentication();
        return new TicketGrantingTicketImpl("123", authentication, new NeverExpiresExpirationPolicy());
    }

    @Test
    public void verifyDeleteTicketWithPGT() {
        final Authentication a = TestUtils.getAuthentication();
        this.infinispanTicketRegistry.addTicket(new TicketGrantingTicketImpl(
                TGT_NAME, a, new NeverExpiresExpirationPolicy()));
        final TicketGrantingTicket tgt = this.infinispanTicketRegistry.getTicket(
                TGT_NAME, TicketGrantingTicket.class);

        final Service service = org.apereo.cas.services.TestUtils.getService("TGT_DELETE_TEST");

        final ServiceTicket st1 = tgt.grantServiceTicket(
                "ST1", service, new NeverExpiresExpirationPolicy(), true, true);

        this.infinispanTicketRegistry.addTicket(st1);

        assertNotNull(this.infinispanTicketRegistry.getTicket(TGT_NAME, TicketGrantingTicket.class));
        assertNotNull(this.infinispanTicketRegistry.getTicket("ST1", ServiceTicket.class));
        final ProxyGrantingTicket pgt = st1.grantProxyGrantingTicket("PGT-1", a, new NeverExpiresExpirationPolicy());
        assertEquals(a, pgt.getAuthentication());

        this.infinispanTicketRegistry.addTicket(pgt);
        assertSame(3, this.infinispanTicketRegistry.deleteTicket(tgt.getId()));

        assertNull(this.infinispanTicketRegistry.getTicket(TGT_NAME, TicketGrantingTicket.class));
        assertNull(this.infinispanTicketRegistry.getTicket("ST1", ServiceTicket.class));
        assertNull(this.infinispanTicketRegistry.getTicket("PGT-1", ProxyGrantingTicket.class));
    }
}

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
        assertTrue(infinispanTicketRegistry.deleteTicket(ticket.getId()));
        assertNull(infinispanTicketRegistry.getTicket(ticket.getId()));
    }

    @Test
    public void deleteTicketOnNonExistingTicketReturnsFalse() {
        final String ticketId = "does_not_exist";
        assertFalse(infinispanTicketRegistry.deleteTicket(ticketId));
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
}

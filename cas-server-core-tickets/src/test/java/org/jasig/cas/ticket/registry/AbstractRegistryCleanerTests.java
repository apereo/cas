package org.jasig.cas.ticket.registry;

import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public abstract class AbstractRegistryCleanerTests {
    protected TicketRegistry ticketRegistry;

    private RegistryCleaner registryCleaner;


    @Before
    public void setUp() throws Exception {
        this.ticketRegistry = this.getNewTicketRegistry();
        this.registryCleaner = this.getNewRegistryCleaner(this.ticketRegistry);
    }

    public abstract RegistryCleaner getNewRegistryCleaner(TicketRegistry newTicketRegistry);

    public abstract TicketRegistry getNewTicketRegistry();

    @Test
    public void verifyCleanEmptyTicketRegistry() {
        clean();
        assertTrue(this.ticketRegistry.getTickets().isEmpty());
    }

    @Test
    public void verifyCleanRegistryOfExpiredTicketsAllExpired() {
        populateRegistryWithExpiredTickets();
        clean();
        assertTrue(this.ticketRegistry.getTickets().isEmpty());
    }

    @Test
    public void verifyCleanRegistryOneNonExpired() {
        populateRegistryWithExpiredTickets();
        final TicketGrantingTicket ticket = new TicketGrantingTicketImpl("testNoExpire",
                org.jasig.cas.authentication.TestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        this.ticketRegistry.addTicket(ticket);
        clean();
        assertEquals(this.ticketRegistry.getTickets().size(), 1);
    }

    protected void populateRegistryWithExpiredTickets() {
        for (int i = 0; i < 10; i++) {
            final TicketGrantingTicket ticket = new TicketGrantingTicketImpl("test" + i,
                    org.jasig.cas.authentication.TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy());
            ticket.markTicketExpired();
            this.ticketRegistry.addTicket(ticket);
        }
    }

    private void clean() {
        beforeCleaning();
        afterCleaning(this.registryCleaner.clean());
    }
    protected void beforeCleaning() {}
    protected void afterCleaning(final Collection<Ticket> removedCol) {}
}

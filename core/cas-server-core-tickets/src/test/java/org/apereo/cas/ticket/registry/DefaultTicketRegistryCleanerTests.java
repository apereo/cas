package org.apereo.cas.ticket.registry;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultTicketRegistryCleanerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class DefaultTicketRegistryCleanerTests {

    @Test
    public void verifyAction() {
        final var logoutManager = mock(LogoutManager.class);
        final TicketRegistry ticketRegistry = new DefaultTicketRegistry();
        final var casuser = new MockTicketGrantingTicket("casuser");
        casuser.markTicketExpired();
        ticketRegistry.addTicket(casuser);
        assertTrue(ticketRegistry.getTickets().size() == 1);
        final var c = new DefaultTicketRegistryCleaner(new NoOpLockingStrategy(), logoutManager, ticketRegistry);
        c.clean();
        assertTrue(ticketRegistry.sessionCount() == 0);
    }
}

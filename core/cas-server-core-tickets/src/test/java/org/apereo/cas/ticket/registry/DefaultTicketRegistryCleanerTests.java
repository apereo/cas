package org.apereo.cas.ticket.registry;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.mock.MockTicketGrantingTicket;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
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
        val logoutManager = mock(LogoutManager.class);
        val ticketRegistry = new DefaultTicketRegistry();
        val casuser = new MockTicketGrantingTicket("casuser");
        casuser.markTicketExpired();
        ticketRegistry.addTicket(casuser);
        assertTrue(ticketRegistry.getTickets().size() == 1);
        val c = new DefaultTicketRegistryCleaner(new NoOpLockingStrategy(), logoutManager, ticketRegistry);
        c.clean();
        assertTrue(ticketRegistry.sessionCount() == 0);
    }
}

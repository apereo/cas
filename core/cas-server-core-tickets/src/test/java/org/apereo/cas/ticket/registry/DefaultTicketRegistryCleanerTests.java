package org.apereo.cas.ticket.registry;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.ticket.registry.support.LockingStrategy;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultTicketRegistryCleanerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Simple")
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

    @Test
    public void verifyNoLock() {
        val logoutManager = mock(LogoutManager.class);
        val ticketRegistry = new DefaultTicketRegistry();
        val lock = mock(LockingStrategy.class);
        when(lock.acquire()).thenReturn(Boolean.FALSE);
        val c = new DefaultTicketRegistryCleaner(lock, logoutManager, ticketRegistry);
        assertTrue(c.clean() == 0);
    }

    @Test
    public void verifyCleanFail() {
        val logoutManager = mock(LogoutManager.class);
        val ticketRegistry = mock(TicketRegistry.class);
        when(ticketRegistry.getTicketsStream()).thenThrow(IllegalArgumentException.class);
        val c = new DefaultTicketRegistryCleaner(new NoOpLockingStrategy(), logoutManager, ticketRegistry);
        assertTrue(c.clean() == 0);
    }

    @Test
    public void verifyNoCleaner() {
        val logoutManager = mock(LogoutManager.class);
        val ticketRegistry = new DefaultTicketRegistry();
        val c = new DefaultTicketRegistryCleaner(new NoOpLockingStrategy(), logoutManager, ticketRegistry) {
            private static final long serialVersionUID = 384489569613368096L;

            @Override
            protected boolean isCleanerSupported() {
                return false;
            }
        };
        assertTrue(c.clean() == 0);
    }
}

package org.apereo.cas.ticket.registry;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.DefaultTicketCatalog;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.lock.LockRepository;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultTicketRegistryCleanerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Tickets")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseTicketRegistryTests.SharedTestConfiguration.class)
class DefaultTicketRegistryCleanerTests {

    @Test
    void verifyAction() throws Throwable {
        val logoutManager = mock(LogoutManager.class);
        val ticketRegistry = newTicketRegistry();
        val tgt = new MockTicketGrantingTicket("casuser");
        tgt.setExpirationPolicy(new HardTimeoutExpirationPolicy(1));
        ticketRegistry.addTicket(tgt);
        assertEquals(1, ticketRegistry.getTickets().size());
        val cleaner = new DefaultTicketRegistryCleaner(LockRepository.noOp(), logoutManager, ticketRegistry);
        tgt.markTicketExpired();
        cleaner.clean();
        assertEquals(0, ticketRegistry.sessionCount());
    }

    @Test
    void verifyLogoutFail() throws Throwable {
        val logoutManager = mock(LogoutManager.class);
        val ticketRegistry = newTicketRegistry();
        val tgt = new MockTicketGrantingTicket("casuser");
        tgt.setExpirationPolicy(new HardTimeoutExpirationPolicy(1));
        ticketRegistry.addTicket(tgt);
        when(logoutManager.performLogout(any())).thenThrow(IllegalArgumentException.class);
        val cleaner = new DefaultTicketRegistryCleaner(LockRepository.noOp(), logoutManager, ticketRegistry);
        assertEquals(1, cleaner.cleanTicket(tgt));
    }

    @Test
    void verifyCleanFail() throws Throwable {
        val logoutManager = mock(LogoutManager.class);
        val ticketRegistry = mock(TicketRegistry.class);
        when(ticketRegistry.stream()).thenThrow(IllegalArgumentException.class);
        val cleaner = new DefaultTicketRegistryCleaner(LockRepository.noOp(), logoutManager, ticketRegistry);
        assertEquals(0, cleaner.clean());
    }

    @Test
    void verifyNoCleaner() throws Throwable {
        val logoutManager = mock(LogoutManager.class);
        val ticketRegistry = newTicketRegistry();
        val cleaner = new DefaultTicketRegistryCleaner(LockRepository.noOp(), logoutManager, ticketRegistry) {
            @Override
            protected boolean isCleanerSupported() {
                return false;
            }
        };
        assertEquals(0, cleaner.clean());
    }

    private static TicketRegistry newTicketRegistry() {
        return new DefaultTicketRegistry(mock(TicketSerializationManager.class), new DefaultTicketCatalog());
    }

}

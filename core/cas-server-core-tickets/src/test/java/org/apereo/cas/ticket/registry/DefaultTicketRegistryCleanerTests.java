package org.apereo.cas.ticket.registry;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.events.logout.CasRequestSingleLogoutEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketDestroyedEvent;
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
import org.springframework.context.ConfigurableApplicationContext;

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
        val applicationContext = mock(ConfigurableApplicationContext.class);
        val ticketRegistry = newTicketRegistry();
        val tgt = new MockTicketGrantingTicket("casuser");
        tgt.setExpirationPolicy(new HardTimeoutExpirationPolicy(1));
        ticketRegistry.addTicket(tgt);
        assertEquals(1, ticketRegistry.getTickets().size());
        val cleaner = new DefaultTicketRegistryCleaner(LockRepository.noOp(), applicationContext, ticketRegistry);
        tgt.markTicketExpired();
        cleaner.clean();
        assertEquals(0, ticketRegistry.sessionCount());
        verify(applicationContext).publishEvent(any(CasRequestSingleLogoutEvent.class));
        verify(applicationContext).publishEvent(any(CasTicketGrantingTicketDestroyedEvent.class));
    }

    @Test
    void verifyCleanFail() throws Throwable {
        val applicationContext = mock(ConfigurableApplicationContext.class);
        val ticketRegistry = mock(TicketRegistry.class);
        when(ticketRegistry.stream()).thenThrow(IllegalArgumentException.class);
        val cleaner = new DefaultTicketRegistryCleaner(LockRepository.noOp(), applicationContext, ticketRegistry);
        assertEquals(0, cleaner.clean());
    }

    @Test
    void verifyNoCleaner() throws Throwable {
        val applicationContext = mock(ConfigurableApplicationContext.class);
        val ticketRegistry = newTicketRegistry();
        val cleaner = new DefaultTicketRegistryCleaner(LockRepository.noOp(), applicationContext, ticketRegistry) {
            @Override
            protected boolean isCleanerSupported() {
                return false;
            }
        };
        assertEquals(0, cleaner.clean());
    }

    private static TicketRegistry newTicketRegistry() {
        return new DefaultTicketRegistry(mock(TicketSerializationManager.class), new DefaultTicketCatalog(),
                mock(ConfigurableApplicationContext.class));
    }

}

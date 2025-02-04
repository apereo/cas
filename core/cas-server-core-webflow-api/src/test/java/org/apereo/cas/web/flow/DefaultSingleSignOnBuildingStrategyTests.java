package org.apereo.cas.web.flow;

import lombok.val;
import org.apereo.cas.support.events.logout.CasRequestSingleLogoutEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketDestroyedEvent;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultSingleSignOnBuildingStrategyTests}.
 *
 * @author Brian Kerr
 * @since 7.2.0
 */
@Tag("Webflow")
public class DefaultSingleSignOnBuildingStrategyTests {

    @Test
    void verifyRemoveTicketGrantingTicket() throws Throwable {
        val ticketRegistrySupport = mock(TicketRegistrySupport.class);
        val ticketRegistry = mock(TicketRegistry.class);
        val applicationContext = mock(ConfigurableApplicationContext.class);
        val singleSignOnBuildingStrategy = new DefaultSingleSignOnBuildingStrategy(ticketRegistrySupport, null, applicationContext);
        val ticketGrantingTicket = mock(TicketGrantingTicket.class);

        when(ticketRegistrySupport.getTicketRegistry()).thenReturn(ticketRegistry);
        when(ticketRegistrySupport.getTicket("TGT-1")).thenReturn(ticketGrantingTicket);

        singleSignOnBuildingStrategy.removeTicketGrantingTicket("TGT-1");

        verify(applicationContext, times(1)).publishEvent(any(CasRequestSingleLogoutEvent.class));
        verify(ticketRegistry, times(1)).deleteTicket("TGT-1");
        verify(applicationContext, times(1)).publishEvent(any(CasTicketGrantingTicketDestroyedEvent.class));
    }

    @Test
    void verifyRemoveTicketGrantingTicketAndTicketIsNotFound() throws Throwable {
        val ticketRegistrySupport = mock(TicketRegistrySupport.class);
        val ticketRegistry = mock(TicketRegistry.class);
        val applicationContext = mock(ConfigurableApplicationContext.class);
        val singleSignOnBuildingStrategy = new DefaultSingleSignOnBuildingStrategy(ticketRegistrySupport, null, applicationContext);

        when(ticketRegistrySupport.getTicketRegistry()).thenReturn(ticketRegistry);

        singleSignOnBuildingStrategy.removeTicketGrantingTicket("TGT-1");

        verify(applicationContext, never()).publishEvent(any(CasRequestSingleLogoutEvent.class));
        verify(ticketRegistry, never()).deleteTicket("TGT-1");
        verify(applicationContext, never()).publishEvent(any(CasTicketGrantingTicketDestroyedEvent.class));
    }
}

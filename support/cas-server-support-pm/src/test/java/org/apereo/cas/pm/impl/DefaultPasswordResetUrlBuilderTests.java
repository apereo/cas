package org.apereo.cas.pm.impl;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultPasswordResetUrlBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("PasswordOps")
class DefaultPasswordResetUrlBuilderTests {

    @Test
    void verifyUrlCarriesStoredTicketId() throws Throwable {
        val passwordManagementService = mock(PasswordManagementService.class);
        when(passwordManagementService.createToken(any())).thenReturn("reset-token");

        val createdTicket = mock(TransientSessionTicket.class);
        when(createdTicket.getId()).thenReturn("TST-created");
        val transientFactory = mock(TransientSessionTicketFactory.class);
        when(transientFactory.create(ArgumentMatchers.<Service>isNull(), anyMap())).thenReturn(createdTicket);
        val ticketFactory = mock(TicketFactory.class);
        when(ticketFactory.get(TransientSessionTicket.class)).thenReturn(transientFactory);

        val storedTicket = mock(Ticket.class);
        when(storedTicket.getId()).thenReturn("TST-stored");
        val ticketRegistry = mock(TicketRegistry.class);
        when(ticketRegistry.addTicket(createdTicket)).thenReturn(storedTicket);

        val casProperties = new CasConfigurationProperties();
        casProperties.getServer().setPrefix("https://sso.example.org/cas");
        val builder = new DefaultPasswordResetUrlBuilder(passwordManagementService, ticketRegistry, ticketFactory, casProperties);
        val url = Objects.requireNonNull(builder.build("casuser")).toExternalForm();
        assertTrue(url.endsWith(PasswordManagementService.PARAMETER_PASSWORD_RESET_TOKEN + "=TST-stored"), url);
        verify(ticketRegistry).addTicket(createdTicket);
    }
}

package org.apereo.cas.ticket.serialization;

import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSerializationConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultTicketStringSerializationManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    MailSenderAutoConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsSerializationConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreUtilConfiguration.class
})
@Tag("Simple")
public class DefaultTicketStringSerializationManagerTests {
    @Autowired
    @Qualifier("ticketSerializationManager")
    private TicketSerializationManager ticketSerializationManager;

    @Autowired
    @Qualifier("defaultTicketFactory")
    private TicketFactory defaultTicketFactory;

    @Test
    public void verifyOperation() {
        val factory = (TicketGrantingTicketFactory) this.defaultTicketFactory.get(TicketGrantingTicket.class);
        val ticket = factory.create(RegisteredServiceTestUtils.getAuthentication(), TicketGrantingTicket.class);
        val result = ticketSerializationManager.serializeTicket(ticket);
        assertNotNull(result);
        val deserializedTicket = ticketSerializationManager.deserializeTicket(result, TicketGrantingTicket.class);
        assertNotNull(deserializedTicket);
    }

    @Test
    public void verifyBadClass() {
        assertThrows(IllegalArgumentException.class, () -> ticketSerializationManager.serializeTicket(mock(Ticket.class)));
        assertThrows(InvalidTicketException.class, () -> ticketSerializationManager.deserializeTicket(StringUtils.EMPTY, StringUtils.EMPTY));
        assertThrows(IllegalArgumentException.class, () -> ticketSerializationManager.deserializeTicket(StringUtils.EMPTY, "something"));
        assertThrows(IllegalArgumentException.class, () -> ticketSerializationManager.deserializeTicket(StringUtils.EMPTY, mock(Ticket.class).getClass()));
    }
}

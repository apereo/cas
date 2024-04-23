package org.apereo.cas.ticket.serialization;

import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
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
    WebMvcAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class
})
@Tag("Tickets")
class DefaultTicketStringSerializationManagerTests {
    @Autowired
    @Qualifier(TicketSerializationManager.BEAN_NAME)
    private TicketSerializationManager ticketSerializationManager;

    @Autowired
    @Qualifier(TicketFactory.BEAN_NAME)
    private TicketFactory defaultTicketFactory;

    @Test
    void verifyOperation() throws Throwable {
        val factory = (TicketGrantingTicketFactory) this.defaultTicketFactory.get(TicketGrantingTicket.class);
        val ticket = factory.create(RegisteredServiceTestUtils.getAuthentication(),
            RegisteredServiceTestUtils.getService(), TicketGrantingTicket.class);
        val result = ticketSerializationManager.serializeTicket(ticket);
        assertNotNull(result);
        val deserializedTicket = ticketSerializationManager.deserializeTicket(result, TicketGrantingTicket.class);
        assertNotNull(deserializedTicket);

        assertThrows(InvalidTicketException.class, () -> ticketSerializationManager.deserializeTicket(result, ProxyTicket.class));
    }

    @Test
    void verifyBadClass() throws Throwable {
        assertThrows(NullPointerException.class, () -> ticketSerializationManager.serializeTicket(mock(Ticket.class)));
        assertThrows(InvalidTicketException.class, () -> ticketSerializationManager.deserializeTicket(StringUtils.EMPTY, StringUtils.EMPTY));
        assertThrows(IllegalArgumentException.class, () -> ticketSerializationManager.deserializeTicket(StringUtils.EMPTY, "something"));
        assertThrows(NullPointerException.class, () -> ticketSerializationManager.deserializeTicket(StringUtils.EMPTY, mock(Ticket.class).getClass()));
    }
}

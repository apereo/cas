package org.apereo.cas.ticket;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.factory.BaseTicketFactoryTests;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultTicketCatalogTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = BaseTicketFactoryTests.SharedTestConfiguration.class)
@Tag("Tickets")
@ExtendWith(CasTestExtension.class)
class DefaultTicketCatalogTests {
    @Autowired
    @Qualifier(TicketCatalog.BEAN_NAME)
    private TicketCatalog ticketCatalog;

    @Autowired
    @Qualifier(TicketTrackingPolicy.BEAN_NAME_SERVICE_TICKET_TRACKING)
    private TicketTrackingPolicy serviceTicketSessionTrackingPolicy;

    @Test
    void verifyFindAll() {
        val tickets = ticketCatalog.findAll();
        assertFalse(tickets.isEmpty());
        assertEquals(5, tickets.size());
    }

    @Test
    void verifyByTicketType() {
        assertTrue(ticketCatalog.findTicketDefinition(TicketGrantingTicket.class).isPresent());
        assertTrue(ticketCatalog.findTicketDefinition(ProxyGrantingTicket.class).isPresent());
        assertTrue(ticketCatalog.findTicketDefinition(ProxyTicket.class).isPresent());
        assertTrue(ticketCatalog.findTicketDefinition(ServiceTicket.class).isPresent());
        assertTrue(ticketCatalog.findTicketDefinition(TransientSessionTicket.class).isPresent());
    }

    @Test
    void verifyUpdateAndFind() {
        val defn = ticketCatalog.findTicketDefinition(TicketGrantingTicket.class).get();
        ticketCatalog.update(defn);
        assertTrue(ticketCatalog.contains(defn.getPrefix()));
    }

    @Test
    void verifyContains() throws Throwable {
        val tgt = new MockTicketGrantingTicket("casuser");
        assertTrue(ticketCatalog.contains(tgt.getPrefix()));
        assertNotNull(ticketCatalog.find(tgt));
        assertNotNull(ticketCatalog.find(tgt.getId()));
        assertNotNull(tgt.getClass());
        val st = tgt.grantServiceTicket(CoreAuthenticationTestUtils.getService(), serviceTicketSessionTrackingPolicy);
        assertTrue(ticketCatalog.contains(st.getPrefix()));
        assertNotNull(ticketCatalog.find(st));
        assertNotNull(ticketCatalog.find(st.getId()));
        assertNotNull(st.getClass());
    }
}

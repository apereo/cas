package org.apereo.cas.web.report;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryQueryCriteria;
import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TicketRegistryEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@TestPropertySource(properties = "management.endpoint.ticketRegistry.access=UNRESTRICTED")
@Tag("ActuatorEndpoint")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TicketRegistryEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("ticketRegistryEndpoint")
    private TicketRegistryEndpoint ticketRegistryEndpoint;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Test
    @Order(0)
    void verifyOperationByType() {
        val criteria = TicketRegistryQueryCriteria.builder().type(TicketGrantingTicket.PREFIX).build();
        val results = ticketRegistryEndpoint.query(criteria);
        assertTrue(results.isEmpty());
    }

    @Test
    void verifyOperationById() throws Throwable {
        val ticket = new MockTicketGrantingTicket(UUID.randomUUID().toString());
        ticketRegistry.addTicket(ticket);
        val criteria = TicketRegistryQueryCriteria.builder()
            .id(ticket.getId())
            .type(TicketGrantingTicket.PREFIX).build();
        val results = ticketRegistryEndpoint.query(criteria);
        assertFalse(results.isEmpty());
    }

    @Test
    void verifyHead() {
        assertTrue(ticketRegistryEndpoint.head().getStatusCode().is2xxSuccessful());
    }

    @Test
    void verifyClean() throws Throwable {
        val ticket = new MockTicketGrantingTicket(UUID.randomUUID().toString());
        ticketRegistry.addTicket(ticket);
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));
        ticket.markTicketExpired();
        val results = (Map) ticketRegistryEndpoint.clean().getBody();
        assertNotNull(results);
        assertTrue(results.containsKey("removed"));
        assertTrue(results.containsKey("total"));
        assertTrue(results.containsKey("duration"));
        assertTrue(results.containsKey("startTime"));
        assertTrue(results.containsKey("endTime"));
    }

    @Test
    void verifyCatalog() {
        val catalog = (List) ticketRegistryEndpoint.ticketCatalog().getBody();
        assertFalse(catalog.isEmpty());
    }
}

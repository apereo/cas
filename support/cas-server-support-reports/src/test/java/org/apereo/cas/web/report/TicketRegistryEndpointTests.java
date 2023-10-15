package org.apereo.cas.web.report;

import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistryQueryCriteria;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TicketRegistryEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@TestPropertySource(properties = "management.endpoint.ticketRegistry.enabled=true")
@Tag("ActuatorEndpoint")
public class TicketRegistryEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("ticketRegistryEndpoint")
    private TicketRegistryEndpoint ticketRegistryEndpoint;

    @Test
    void verifyOperation() throws Throwable {
        val results = ticketRegistryEndpoint.query(TicketRegistryQueryCriteria.builder()
            .type(TicketGrantingTicket.PREFIX).build());
        assertTrue(results.isEmpty());
    }
}

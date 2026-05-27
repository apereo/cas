package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link TicketRegistryEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@TestPropertySource(properties = "management.endpoint.ticketRegistry.access=UNRESTRICTED")
@Tag("ActuatorEndpoint")
class TicketRegistryEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Test
    void verifyOperationById() throws Throwable {
        mockMvc.perform(get("/actuator/ticketRegistry/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"%s\"}".formatted(TicketGrantingTicket.PREFIX)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        val ticket = new TicketGrantingTicketImpl("casuser",
            RegisteredServiceTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE);
        ticketRegistry.addTicket(ticket);
        
        mockMvc.perform(get("/actuator/ticketRegistry/query")
                .queryParam("id", ticket.getId())
                .queryParam("type", TicketGrantingTicket.PREFIX)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void verifyHead() throws Throwable {
        mockMvc.perform(head("/actuator/ticketRegistry"))
            .andExpect(status().is2xxSuccessful());
    }

    @Test
    void verifyClean() throws Throwable {
        val ticket = new MockTicketGrantingTicket(UUID.randomUUID().toString());
        ticketRegistry.addTicket(ticket);
        assertNotNull(ticketRegistry.getTicket(ticket.getId()));
        ticket.markTicketExpired();
        mockMvc.perform(delete("/actuator/ticketRegistry/clean")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.removed").exists())
            .andExpect(jsonPath("$.total").exists())
            .andExpect(jsonPath("$.duration").exists())
            .andExpect(jsonPath("$.startTime").exists())
            .andExpect(jsonPath("$.endTime").exists());
    }

    @Test
    void verifyCatalog() throws Throwable {
        mockMvc.perform(get("/actuator/ticketRegistry/ticketCatalog"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isNotEmpty());
    }
}

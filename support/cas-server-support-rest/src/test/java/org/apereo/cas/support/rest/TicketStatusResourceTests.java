package org.apereo.cas.support.rest;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.rest.resources.TicketStatusResource;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link TicketStatusResourceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@ExtendWith(MockitoExtension.class)
@Tag("RestfulApi")
class TicketStatusResourceTests {
    private static final String TICKETS_RESOURCE_URL = "/cas/v1/tickets";

    @Mock
    private TicketRegistry ticketRegistry;

    @InjectMocks
    private TicketStatusResource ticketStatusResource;

    private MockMvc mockMvc;

    @BeforeEach
    void initialize() {
        this.ticketStatusResource = new TicketStatusResource(ticketRegistry);

        this.mockMvc = MockMvcBuilders.standaloneSetup(this.ticketStatusResource)
            .defaultRequest(get("/")
                .contextPath("/cas")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .build();
    }

    @Test
    void verifyStatus() throws Throwable {
        val tgt = new MockTicketGrantingTicket("casuser");
        when(ticketRegistry.getTicket(anyString(), (Class<Ticket>) any())).thenReturn(tgt);
        this.mockMvc.perform(get(TICKETS_RESOURCE_URL + "/TGT-1"))
            .andExpect(status().isOk())
            .andExpect(content().string(tgt.getId()));
    }

    @Test
    void verifyStatusNotFound() throws Throwable {
        when(ticketRegistry.getTicket(anyString(), (Class<Ticket>) any())).thenThrow(InvalidTicketException.class);
        this.mockMvc.perform(get(TICKETS_RESOURCE_URL + "/TGT-1"))
            .andExpect(status().isNotFound());
    }

    @Test
    void verifyStatusError() throws Throwable {
        when(ticketRegistry.getTicket(anyString())).thenThrow(RuntimeException.class);
        this.mockMvc.perform(get(TICKETS_RESOURCE_URL + "/TGT-1"))
            .andExpect(status().isInternalServerError());
    }
}

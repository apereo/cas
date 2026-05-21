package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link SingleSignOnSessionStatusEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@TestPropertySource(properties = {
    "cas.tgc.crypto.enabled=false",
    "management.endpoint.sso.access=UNRESTRICTED"
})
@Tag("ActuatorEndpoint")
class SingleSignOnSessionStatusEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
    private CasCookieBuilder ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Test
    void verifyOperationByValue() throws Throwable {
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        mockMvc.perform(get("/actuator/sso")
                .param("tgc", tgt.getId())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is2xxSuccessful());
    }

    @Test
    void verifyOperation() throws Throwable {
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        val fakeRequest = new MockHttpServletRequest();
        val fakeResponse = new MockHttpServletResponse();
        ticketGrantingTicketCookieGenerator.addCookie(fakeRequest, fakeResponse, tgt.getId());
        val tgcCookie = fakeResponse.getCookies()[0];
        mockMvc.perform(get("/actuator/sso")
                .cookie(new jakarta.servlet.http.Cookie(tgcCookie.getName(), tgcCookie.getValue()))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.principal").exists())
            .andExpect(jsonPath("$.authenticationDate").exists())
            .andExpect(jsonPath("$.ticketGrantingTicketCreationTime").exists())
            .andExpect(jsonPath("$.ticketGrantingTicketPreviousTimeUsed").exists())
            .andExpect(jsonPath("$.ticketGrantingTicketLastTimeUsed").exists());
    }

    @Test
    void verifyNoTicket() throws Throwable {
        mockMvc.perform(get("/actuator/sso")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError());

        val tgt = new MockTicketGrantingTicket("casuser");
        val fakeRequest = new MockHttpServletRequest();
        val fakeResponse = new MockHttpServletResponse();
        ticketGrantingTicketCookieGenerator.addCookie(fakeRequest, fakeResponse, tgt.getId());
        val tgcCookie = fakeResponse.getCookies()[0];
        mockMvc.perform(get("/actuator/sso")
                .cookie(new jakarta.servlet.http.Cookie(tgcCookie.getName(), tgcCookie.getValue()))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError());
    }
}

package org.apereo.cas.web.report;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

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
    @Qualifier("singleSignOnSessionStatusEndpoint")
    private SingleSignOnSessionStatusEndpoint singleSignOnSessionStatusEndpoint;

    @Autowired
    @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
    private CasCookieBuilder ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Test
    void verifyOperationByValue() throws Throwable {
        val request = new MockHttpServletRequest();
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        val entity = singleSignOnSessionStatusEndpoint.ssoStatus(tgt.getId(), request);
        assertTrue(entity.getStatusCode().is2xxSuccessful());
    }

    @Test
    void verifyOperation() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        ticketGrantingTicketCookieGenerator.addCookie(request, response, tgt.getId());
        request.setCookies(response.getCookies());
        val entity = singleSignOnSessionStatusEndpoint.ssoStatus(null, request);
        assertTrue(entity.getStatusCode().is2xxSuccessful());
        val body = Objects.requireNonNull((Map) entity.getBody());
        assertTrue(body.containsKey("principal"));
        assertTrue(body.containsKey("authenticationDate"));
        assertTrue(body.containsKey("ticketGrantingTicketCreationTime"));
        assertTrue(body.containsKey("ticketGrantingTicketPreviousTimeUsed"));
        assertTrue(body.containsKey("ticketGrantingTicketLastTimeUsed"));
    }

    @Test
    void verifyNoTicket() {
        val request = new MockHttpServletRequest();
        assertTrue(singleSignOnSessionStatusEndpoint.ssoStatus(null, request).getStatusCode().is4xxClientError());
        val response = new MockHttpServletResponse();
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketGrantingTicketCookieGenerator.addCookie(request, response, tgt.getId());
        request.setCookies(response.getCookies());
        assertTrue(singleSignOnSessionStatusEndpoint.ssoStatus(null, request).getStatusCode().is4xxClientError());
    }
}

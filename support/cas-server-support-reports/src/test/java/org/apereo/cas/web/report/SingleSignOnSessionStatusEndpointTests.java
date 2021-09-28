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
    "management.endpoint.sso.enabled=true"
})
@Tag("ActuatorEndpoint")
public class SingleSignOnSessionStatusEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("singleSignOnSessionStatusEndpoint")
    private SingleSignOnSessionStatusEndpoint singleSignOnSessionStatusEndpoint;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private CasCookieBuilder ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Test
    public void verifyOperation() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        ticketGrantingTicketCookieGenerator.addCookie(response, tgt.getId());
        request.setCookies(response.getCookies());
        val entity = singleSignOnSessionStatusEndpoint.ssoStatus(request);
        assertTrue(entity.getStatusCode().is2xxSuccessful());
        val body = Objects.requireNonNull(Map.class.cast(entity.getBody()));
        assertTrue(body.containsKey("principal"));
        assertTrue(body.containsKey("authenticationDate"));
        assertTrue(body.containsKey("ticketGrantingTicketCreationTime"));
        assertTrue(body.containsKey("ticketGrantingTicketPreviousTimeUsed"));
        assertTrue(body.containsKey("ticketGrantingTicketLastTimeUsed"));
    }

    @Test
    public void verifyNoTicket() {
        val request = new MockHttpServletRequest();
        assertTrue(singleSignOnSessionStatusEndpoint.ssoStatus(request).getStatusCode().is4xxClientError());

        val response = new MockHttpServletResponse();
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketGrantingTicketCookieGenerator.addCookie(response, tgt.getId());
        request.setCookies(response.getCookies());
        assertTrue(singleSignOnSessionStatusEndpoint.ssoStatus(request).getStatusCode().is4xxClientError());
    }
}

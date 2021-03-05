package org.apereo.cas.web.report;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SingleSignOnSessionsEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = "management.endpoint.ssoSessions.enabled=true")
@Tag("ActuatorEndpoint")
public class SingleSignOnSessionsEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("singleSignOnSessionsEndpoint")
    private SingleSignOnSessionsEndpoint singleSignOnSessionsEndpoint;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @BeforeEach
    public void setup() {
        val result = CoreAuthenticationTestUtils.getAuthenticationResult();
        val tgt = centralAuthenticationService.createTicketGrantingTicket(result);
        val st = centralAuthenticationService.grantServiceTicket(tgt.getId(), CoreAuthenticationTestUtils.getWebApplicationService(), result);
        assertNotNull(st);
    }

    @Test
    public void verifyDelete() {
        var results = singleSignOnSessionsEndpoint.destroySsoSessions(null, null);
        assertEquals(HttpStatus.BAD_REQUEST.value(), results.get("status"));
        results = singleSignOnSessionsEndpoint.destroySsoSessions(null, CoreAuthenticationTestUtils.CONST_USERNAME);
        assertFalse(results.isEmpty());

        results = singleSignOnSessionsEndpoint.destroySsoSession("unknown-ticket");
        assertTrue(results.containsKey("status"));
        assertTrue(results.containsKey("message"));
        assertTrue(results.containsKey("ticketGrantingTicket"));

        val authResult = CoreAuthenticationTestUtils.getAuthenticationResult();
        centralAuthenticationService.createTicketGrantingTicket(authResult);
        results = singleSignOnSessionsEndpoint.destroySsoSessions(SingleSignOnSessionsEndpoint.SsoSessionReportOptions.ALL.getType(), null);
        assertFalse(results.isEmpty());
    }

    @Test
    public void verifyOperation() {
        var results = singleSignOnSessionsEndpoint.getSsoSessions(SingleSignOnSessionsEndpoint.SsoSessionReportOptions.ALL.getType());
        assertFalse(results.isEmpty());
        assertTrue(results.containsKey("totalUsageCount"));
        assertTrue(results.containsKey("activeSsoSessions"));
        assertTrue(results.containsKey("totalTicketGrantingTickets"));
        assertTrue(results.containsKey("totalTickets"));
        assertTrue(results.containsKey("totalPrincipals"));
        assertTrue(results.containsKey("totalProxyGrantingTickets"));

        val sessions = (List) results.get("activeSsoSessions");
        assertEquals(1, sessions.size());

        val tgt = Map.class.cast(sessions.get(0))
            .get(SingleSignOnSessionsEndpoint.SsoSessionAttributeKeys.TICKET_GRANTING_TICKET.getAttributeKey()).toString();
        results = singleSignOnSessionsEndpoint.destroySsoSession(tgt);
        assertFalse(results.isEmpty());
        assertTrue(results.containsKey("status"));
        assertTrue(results.containsKey("ticketGrantingTicket"));

        results = singleSignOnSessionsEndpoint.destroySsoSessions(SingleSignOnSessionsEndpoint.SsoSessionReportOptions.ALL.getType(), null);
        assertEquals(1, results.size());
        assertTrue(results.containsKey("status"));
    }

    @Test
    public void verifyProxies() {
        val tgt = new MockTicketGrantingTicket("casuser");
        tgt.setProxiedBy(CoreAuthenticationTestUtils.getWebApplicationService());
        centralAuthenticationService.addTicket(tgt);
        val results = singleSignOnSessionsEndpoint.getSsoSessions(SingleSignOnSessionsEndpoint.SsoSessionReportOptions.ALL.getType());
        assertFalse(results.isEmpty());
    }

    @Test
    public void verifyDirect() {
        val tgt = new MockTicketGrantingTicket("casuser");
        tgt.setProxiedBy(CoreAuthenticationTestUtils.getWebApplicationService());
        centralAuthenticationService.addTicket(tgt);
        val results = singleSignOnSessionsEndpoint.getSsoSessions(SingleSignOnSessionsEndpoint.SsoSessionReportOptions.DIRECT.getType());
        assertFalse(results.isEmpty());
    }

    @Test
    public void verifyDeleteFails() {
        val cas = mock(CentralAuthenticationService.class);
        when(cas.getTickets(any(Predicate.class))).thenReturn(List.of(new MockTicketGrantingTicket("casuser")));
        when(cas.deleteTicket(anyString())).thenThrow(new RuntimeException());

        val results = new SingleSignOnSessionsEndpoint(cas, casProperties).destroySsoSessions(
            SingleSignOnSessionsEndpoint.SsoSessionReportOptions.DIRECT.getType(), null);
        assertFalse(results.isEmpty());
        assertTrue(results.containsKey("failedTicketGrantingTickets"));
    }
}


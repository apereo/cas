package org.apereo.cas.web.report;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SingleSignOnSessionsEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = "management.endpoint.ssoSessions.enabled=true")
@Tag("Simple")
public class SingleSignOnSessionsEndpointTests extends AbstractCasEndpointTests {
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
        val st = centralAuthenticationService.grantServiceTicket(tgt.getId(), CoreAuthenticationTestUtils.getService(), result);
        assertNotNull(st);
    }

    @Test
    public void verifyDelete() {
        val results = singleSignOnSessionsEndpoint.destroySsoSessions(null, CoreAuthenticationTestUtils.CONST_USERNAME);
        assertEquals(1, results.size());
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
}


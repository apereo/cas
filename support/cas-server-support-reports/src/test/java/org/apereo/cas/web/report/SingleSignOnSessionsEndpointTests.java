package org.apereo.cas.web.report;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.spring.DirectObjectProvider;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SingleSignOnSessionsEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = "management.endpoint.ssoSessions.access=UNRESTRICTED")
@Tag("ActuatorEndpoint")
@Execution(ExecutionMode.SAME_THREAD)
class SingleSignOnSessionsEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("singleSignOnSessionsEndpoint")
    private SingleSignOnSessionsEndpoint singleSignOnSessionsEndpoint;

    @Autowired
    @Qualifier(SingleLogoutRequestExecutor.BEAN_NAME)
    private SingleLogoutRequestExecutor defaultSingleLogoutRequestExecutor;

    @Autowired
    @Qualifier(CentralAuthenticationService.BEAN_NAME)
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @BeforeEach
    void setup() throws Throwable {
        val result = CoreAuthenticationTestUtils.getAuthenticationResult();
        val tgt = centralAuthenticationService.createTicketGrantingTicket(result);
        val st = centralAuthenticationService.grantServiceTicket(tgt.getId(), CoreAuthenticationTestUtils.getWebApplicationService(), result);
        assertNotNull(st);
    }

    @AfterEach
    public void teardown() {
        singleSignOnSessionsEndpoint.destroySsoSessions(
            new SingleSignOnSessionsEndpoint.SsoSessionsRequest()
                .withType(SingleSignOnSessionsEndpoint.SsoSessionReportOptions.ALL.getType()),
            new MockHttpServletRequest(), new MockHttpServletResponse());
    }

    @Test
    void verifyDelete() throws Throwable {
        var results = singleSignOnSessionsEndpoint.destroySsoSessions(
            new SingleSignOnSessionsEndpoint.SsoSessionsRequest().withType(null),
            new MockHttpServletRequest(), new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST.value(), results.get("status"));
        results = singleSignOnSessionsEndpoint.destroySsoSessions(
            new SingleSignOnSessionsEndpoint.SsoSessionsRequest().withUsername(CoreAuthenticationTestUtils.CONST_USERNAME),
            new MockHttpServletRequest(), new MockHttpServletResponse());
        assertFalse(results.isEmpty());

        results = singleSignOnSessionsEndpoint.destroySsoSession("unknown-ticket",
            new MockHttpServletRequest(), new MockHttpServletResponse());
        assertTrue(results.containsKey("status"));
        assertTrue(results.containsKey("ticketGrantingTicket"));

        val authResult = CoreAuthenticationTestUtils.getAuthenticationResult();
        val tgt = centralAuthenticationService.createTicketGrantingTicket(authResult);
        assertNotNull(tgt);
        results = singleSignOnSessionsEndpoint.destroySsoSessions(
            new SingleSignOnSessionsEndpoint.SsoSessionsRequest().withType(SingleSignOnSessionsEndpoint.SsoSessionReportOptions.ALL.getType()),
            new MockHttpServletRequest(), new MockHttpServletResponse());
        assertFalse(results.isEmpty());
        assertNotNull(singleSignOnSessionsEndpoint.toString());
        assertTrue(ticketRegistry.getTickets(ticket -> ticket.getId().equals(tgt.getId()) && !ticket.isExpired()).findAny().isEmpty());
    }

    @Test
    void verifyOperation() {
        var results = singleSignOnSessionsEndpoint.getSsoSessions(new SingleSignOnSessionsEndpoint.SsoSessionsRequest()
            .withType(SingleSignOnSessionsEndpoint.SsoSessionReportOptions.ALL.getType()));
        assertFalse(results.isEmpty());
        assertTrue(results.containsKey("activeSsoSessions"));
        
        val sessions = (List) results.get("activeSsoSessions");
        assertEquals(1, sessions.size());

        val tgt = ((Map) sessions.getFirst())
            .get(SingleSignOnSessionsEndpoint.SsoSessionAttributeKeys.TICKET_GRANTING_TICKET_ID.getAttributeKey()).toString();
        results = singleSignOnSessionsEndpoint.destroySsoSession(tgt, new MockHttpServletRequest(), new MockHttpServletResponse());
        assertFalse(results.isEmpty());
        assertTrue(results.containsKey("status"));
        assertTrue(results.containsKey("ticketGrantingTicket"));

        results = singleSignOnSessionsEndpoint.destroySsoSessions(new SingleSignOnSessionsEndpoint.SsoSessionsRequest()
                .withType(SingleSignOnSessionsEndpoint.SsoSessionReportOptions.ALL.getType()),
            new MockHttpServletRequest(), new MockHttpServletResponse());
        assertEquals(1, results.size());
        assertTrue(results.containsKey("status"));
    }

    @Test
    void verifyProxies() throws Throwable {
        val tgt = new MockTicketGrantingTicket("casuser");
        tgt.setProxiedBy(CoreAuthenticationTestUtils.getWebApplicationService());
        ticketRegistry.addTicket(tgt);
        var results = singleSignOnSessionsEndpoint.getSsoSessions(new SingleSignOnSessionsEndpoint.SsoSessionsRequest()
            .withType(SingleSignOnSessionsEndpoint.SsoSessionReportOptions.ALL.getType()));
        assertFalse(results.isEmpty());
        results = singleSignOnSessionsEndpoint.getSsoSessions(new SingleSignOnSessionsEndpoint.SsoSessionsRequest());
        assertFalse(results.isEmpty());
    }

    @Test
    void verifyDirect() throws Throwable {
        val tgt = new MockTicketGrantingTicket("casuser");
        tgt.setProxiedBy(CoreAuthenticationTestUtils.getWebApplicationService());
        ticketRegistry.addTicket(tgt);
        var results = singleSignOnSessionsEndpoint.getSsoSessions(new SingleSignOnSessionsEndpoint.SsoSessionsRequest()
            .withType(SingleSignOnSessionsEndpoint.SsoSessionReportOptions.DIRECT.getType()));
        assertFalse(results.isEmpty());
        results = singleSignOnSessionsEndpoint.getSsoSessions(new SingleSignOnSessionsEndpoint.SsoSessionsRequest()
            .withType(SingleSignOnSessionsEndpoint.SsoSessionReportOptions.ALL.getType()));
        assertFalse(results.isEmpty());
    }

    @Test
    void verifyDeleteFails() throws Throwable {
        val registry = mock(TicketRegistry.class);
        when(registry.getTickets(any(Predicate.class))).thenReturn(Stream.of(new MockTicketGrantingTicket("casuser")));
        when(registry.deleteTicket(anyString())).thenThrow(new RuntimeException());

        val results = new SingleSignOnSessionsEndpoint(new DirectObjectProvider<>(registry), applicationContext,
            casProperties, new DirectObjectProvider<>(defaultSingleLogoutRequestExecutor)).destroySsoSessions(
            new SingleSignOnSessionsEndpoint.SsoSessionsRequest()
                .withType(SingleSignOnSessionsEndpoint.SsoSessionReportOptions.DIRECT.getType()),
            new MockHttpServletRequest(), new MockHttpServletResponse());
        assertFalse(results.isEmpty());
    }
}


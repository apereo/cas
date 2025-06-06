package org.apereo.cas.logout;

import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.logout.slo.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.DefaultSingleLogoutServiceMessageHandler;
import org.apereo.cas.logout.slo.SingleLogoutExecutionRequest;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.BaseRegisteredService;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.web.HttpMessage;
import org.apereo.cas.web.SimpleUrlValidatorFactoryBean;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.net.URL;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Jerome Leleu
 * @since 4.0.0
 */
@Tag("Logout")
class DefaultLogoutManagerTests {
    private static final String ID = "id";

    private static final String URL = "https://www.github.com";

    private LogoutManager logoutManager;

    private MockTicketGrantingTicket tgt;

    private AbstractWebApplicationService simpleWebApplicationServiceImpl;

    private BaseRegisteredService registeredService;

    @Mock
    private ServicesManager servicesManager;

    @Mock
    private HttpClient client;

    @Mock
    private TenantExtractor tenantExtractor;
    
    private DefaultSingleLogoutServiceMessageHandler singleLogoutServiceMessageHandler;
    
    private static BaseRegisteredService getRegisteredService(final String id) {
        val service = new CasRegisteredService();
        service.setServiceId(id);
        service.setName("Test registered service " + id);
        service.setDescription("Registered service description");
        service.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy().setPattern("^https?://.+"));
        service.setId(RandomUtils.getNativeInstance().nextInt());
        return service;
    }

    @BeforeEach
    void initialize() throws Exception {
        MockitoAnnotations.openMocks(this).close();

        tgt = new MockTicketGrantingTicket("casuser");

        when(client.isValidEndPoint(any(String.class))).thenReturn(true);
        when(client.isValidEndPoint(any(URL.class))).thenReturn(true);
        when(client.sendMessageToEndPoint(any(HttpMessage.class))).thenReturn(true);

        val validator = new SimpleUrlValidatorFactoryBean(true).getObject();

        singleLogoutServiceMessageHandler = new DefaultSingleLogoutServiceMessageHandler(client,
            new DefaultSingleLogoutMessageCreator(), servicesManager,
            new DefaultSingleLogoutServiceLogoutUrlBuilder(servicesManager, validator), true,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));

        this.simpleWebApplicationServiceImpl = RegisteredServiceTestUtils.getService(URL);
        tgt.getServices().put(ID, this.simpleWebApplicationServiceImpl);

        val plan = new DefaultLogoutExecutionPlan();
        plan.registerSingleLogoutServiceMessageHandler(singleLogoutServiceMessageHandler);

        this.logoutManager = new DefaultLogoutManager(false, plan);
        this.registeredService = getRegisteredService(URL);
        when(servicesManager.findServiceBy(this.simpleWebApplicationServiceImpl)).thenReturn(this.registeredService);
        assertTrue(plan.getLogoutRedirectionStrategies().isEmpty());
    }

    @Test
    void verifyServiceLogoutUrlIsUsed() {

        this.registeredService.setLogoutUrl("https://www.apereo.org");
        val logoutRequests = this.logoutManager.performLogout(
            SingleLogoutExecutionRequest
                .builder()
                .ticketGrantingTicket(tgt)
                .httpServletResponse(Optional.of(new MockHttpServletResponse()))
                .httpServletRequest(Optional.of(new MockHttpServletRequest()))
                .build());
        val logoutRequest = logoutRequests.getFirst();
        assertEquals(this.registeredService.getLogoutUrl(), logoutRequest.getLogoutUrl().toExternalForm());
    }

    @Test
    void verifyLogoutDisabled() {
        val plan = new DefaultLogoutExecutionPlan();
        plan.registerSingleLogoutServiceMessageHandler(singleLogoutServiceMessageHandler);
        this.logoutManager = new DefaultLogoutManager(true, plan);

        val logoutRequests = this.logoutManager.performLogout(SingleLogoutExecutionRequest
            .builder()
            .ticketGrantingTicket(tgt)
            .httpServletResponse(Optional.of(new MockHttpServletResponse()))
            .httpServletRequest(Optional.of(new MockHttpServletRequest()))
            .build());
        assertEquals(0, logoutRequests.size());
    }

    @Test
    void verifyLogoutAlreadyLoggedOut() {
        this.simpleWebApplicationServiceImpl.setLoggedOutAlready(true);
        val logoutRequests = this.logoutManager.performLogout(SingleLogoutExecutionRequest
            .builder()
            .ticketGrantingTicket(tgt)
            .httpServletResponse(Optional.of(new MockHttpServletResponse()))
            .httpServletRequest(Optional.of(new MockHttpServletRequest()))
            .build());
        assertEquals(0, logoutRequests.size());
    }

    @Test
    void verifyLogoutTypeNotSet() {
        val logoutRequests = this.logoutManager.performLogout(SingleLogoutExecutionRequest
            .builder()
            .ticketGrantingTicket(tgt)
            .httpServletResponse(Optional.of(new MockHttpServletResponse()))
            .httpServletRequest(Optional.of(new MockHttpServletRequest()))
            .build());
        assertEquals(1, logoutRequests.size());
        val logoutRequest = logoutRequests.getFirst();
        assertEquals(ID, logoutRequest.getTicketId());
        assertEquals(this.simpleWebApplicationServiceImpl, logoutRequest.getService());
        assertEquals(LogoutRequestStatus.SUCCESS, logoutRequest.getStatus());
    }

    @Test
    void verifyLogoutTypeBack() {
        this.registeredService.setLogoutType(RegisteredServiceLogoutType.BACK_CHANNEL);
        val logoutRequests = this.logoutManager.performLogout(SingleLogoutExecutionRequest
            .builder()
            .ticketGrantingTicket(tgt)
            .httpServletResponse(Optional.of(new MockHttpServletResponse()))
            .httpServletRequest(Optional.of(new MockHttpServletRequest()))
            .build());
        assertEquals(1, logoutRequests.size());
        val logoutRequest = logoutRequests.getFirst();
        assertEquals(ID, logoutRequest.getTicketId());
        assertEquals(this.simpleWebApplicationServiceImpl, logoutRequest.getService());
        assertEquals(LogoutRequestStatus.SUCCESS, logoutRequest.getStatus());
    }

    @Test
    void verifyLogoutTypeNone() {
        this.registeredService.setLogoutType(RegisteredServiceLogoutType.NONE);
        val logoutRequests = this.logoutManager.performLogout(SingleLogoutExecutionRequest
            .builder()
            .ticketGrantingTicket(tgt)
            .httpServletResponse(Optional.of(new MockHttpServletResponse()))
            .httpServletRequest(Optional.of(new MockHttpServletRequest()))
            .build());
        assertEquals(0, logoutRequests.size());
    }

    @Test
    void verifyLogoutTypeNull() {
        this.registeredService.setLogoutType(null);
        val logoutRequests = this.logoutManager.performLogout(SingleLogoutExecutionRequest
            .builder()
            .ticketGrantingTicket(tgt)
            .httpServletResponse(Optional.of(new MockHttpServletResponse()))
            .httpServletRequest(Optional.of(new MockHttpServletRequest()))
            .build());
        assertEquals(1, logoutRequests.size());
        val logoutRequest = logoutRequests.getFirst();
        assertEquals(ID, logoutRequest.getTicketId());
    }

    @Test
    void verifyLogoutTypeFront() {
        this.registeredService.setLogoutType(RegisteredServiceLogoutType.FRONT_CHANNEL);
        val logoutRequests = this.logoutManager.performLogout(SingleLogoutExecutionRequest
            .builder()
            .ticketGrantingTicket(tgt)
            .httpServletResponse(Optional.of(new MockHttpServletResponse()))
            .httpServletRequest(Optional.of(new MockHttpServletRequest()))
            .build());
        assertEquals(1, logoutRequests.size());
        val logoutRequest = logoutRequests.getFirst();
        assertEquals(ID, logoutRequest.getTicketId());
        assertEquals(this.simpleWebApplicationServiceImpl, logoutRequest.getService());
        assertEquals(LogoutRequestStatus.NOT_ATTEMPTED, logoutRequest.getStatus());
    }

    @Test
    void verifyAsynchronousLogout() {
        this.registeredService.setLogoutType(RegisteredServiceLogoutType.BACK_CHANNEL);
        val logoutRequests = this.logoutManager.performLogout(SingleLogoutExecutionRequest
            .builder()
            .ticketGrantingTicket(tgt)
            .httpServletResponse(Optional.of(new MockHttpServletResponse()))
            .httpServletRequest(Optional.of(new MockHttpServletRequest()))
            .build());
        assertEquals(1, logoutRequests.size());
    }
}

package org.apereo.cas.logout;

import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.logout.slo.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.DefaultSingleLogoutServiceMessageHandler;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.HttpMessage;
import org.apereo.cas.web.SimpleUrlValidatorFactoryBean;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Jerome Leleu
 * @since 4.0.0
 */
public class DefaultLogoutManagerTests {
    private static final String ID = "id";
    private static final String URL = "http://www.github.com";

    private LogoutManager logoutManager;

    private TicketGrantingTicket tgt;

    private AbstractWebApplicationService simpleWebApplicationServiceImpl;

    private AbstractRegisteredService registeredService;

    @Mock
    private ServicesManager servicesManager;

    @Mock
    private HttpClient client;

    private DefaultSingleLogoutServiceMessageHandler singleLogoutServiceMessageHandler;

    public DefaultLogoutManagerTests() {
        MockitoAnnotations.initMocks(this);
    }

    @SneakyThrows
    public static AbstractRegisteredService getRegisteredService(final String id) {
        val s = new RegexRegisteredService();
        s.setServiceId(id);
        s.setName("Test registered service " + id);
        s.setDescription("Registered service description");
        s.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("^https?://.+"));
        s.setId(RandomUtils.getNativeInstance().nextInt());
        return s;
    }


    public static AbstractWebApplicationService getService(final String url) {
        val request = new MockHttpServletRequest();
        request.addParameter("service", url);
        return (AbstractWebApplicationService) new WebApplicationServiceFactory().createService(request);
    }

    @BeforeEach
    public void initialize() {
        tgt = new MockTicketGrantingTicket("casuser");

        when(client.isValidEndPoint(any(String.class))).thenReturn(true);
        when(client.isValidEndPoint(any(URL.class))).thenReturn(true);
        when(client.sendMessageToEndPoint(any(HttpMessage.class))).thenReturn(true);

        val validator = new SimpleUrlValidatorFactoryBean(true).getObject();

        singleLogoutServiceMessageHandler = new DefaultSingleLogoutServiceMessageHandler(client,
            new DefaultSingleLogoutMessageCreator(), servicesManager,
            new DefaultSingleLogoutServiceLogoutUrlBuilder(validator), true,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));
        
        this.simpleWebApplicationServiceImpl = getService(URL);
        tgt.getServices().put(ID, this.simpleWebApplicationServiceImpl);

        val plan = new DefaultLogoutExecutionPlan();
        plan.registerSingleLogoutServiceMessageHandler(singleLogoutServiceMessageHandler);

        this.logoutManager = new DefaultLogoutManager(false, plan);
        this.registeredService = getRegisteredService(URL);
        when(servicesManager.findServiceBy(this.simpleWebApplicationServiceImpl)).thenReturn(this.registeredService);
    }

    @Test
    public void verifyServiceLogoutUrlIsUsed() {
        this.registeredService.setLogoutUrl("https://www.apereo.org");
        val logoutRequests = this.logoutManager.performLogout(tgt);
        val logoutRequest = logoutRequests.iterator().next();
        assertEquals(this.registeredService.getLogoutUrl(), logoutRequest.getLogoutUrl().toExternalForm());
    }

    @Test
    public void verifyLogoutDisabled() {
        val plan = new DefaultLogoutExecutionPlan();
        plan.registerSingleLogoutServiceMessageHandler(singleLogoutServiceMessageHandler);
        this.logoutManager = new DefaultLogoutManager(true, plan);

        val logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(0, logoutRequests.size());
    }

    @Test
    public void verifyLogoutAlreadyLoggedOut() {
        this.simpleWebApplicationServiceImpl.setLoggedOutAlready(true);
        val logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(0, logoutRequests.size());
    }

    @Test
    public void verifyLogoutTypeNotSet() {
        val logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(1, logoutRequests.size());
        val logoutRequest = logoutRequests.iterator().next();
        assertEquals(ID, logoutRequest.getTicketId());
        assertEquals(this.simpleWebApplicationServiceImpl, logoutRequest.getService());
        assertEquals(LogoutRequestStatus.SUCCESS, logoutRequest.getStatus());
    }

    @Test
    public void verifyLogoutTypeBack() {
        this.registeredService.setLogoutType(RegisteredServiceLogoutType.BACK_CHANNEL);
        val logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(1, logoutRequests.size());
        val logoutRequest = logoutRequests.iterator().next();
        assertEquals(ID, logoutRequest.getTicketId());
        assertEquals(this.simpleWebApplicationServiceImpl, logoutRequest.getService());
        assertEquals(LogoutRequestStatus.SUCCESS, logoutRequest.getStatus());
    }

    @Test
    public void verifyLogoutTypeNone() {
        this.registeredService.setLogoutType(RegisteredServiceLogoutType.NONE);
        val logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(0, logoutRequests.size());
    }

    @Test
    public void verifyLogoutTypeNull() {
        this.registeredService.setLogoutType(null);
        val logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(1, logoutRequests.size());
        val logoutRequest = logoutRequests.iterator().next();
        assertEquals(ID, logoutRequest.getTicketId());
    }

    @Test
    public void verifyLogoutTypeFront() {
        this.registeredService.setLogoutType(RegisteredServiceLogoutType.FRONT_CHANNEL);
        val logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(1, logoutRequests.size());
        val logoutRequest = logoutRequests.iterator().next();
        assertEquals(ID, logoutRequest.getTicketId());
        assertEquals(this.simpleWebApplicationServiceImpl, logoutRequest.getService());
        assertEquals(LogoutRequestStatus.NOT_ATTEMPTED, logoutRequest.getStatus());
    }

    @Test
    public void verifyAsynchronousLogout() {
        this.registeredService.setLogoutType(RegisteredServiceLogoutType.BACK_CHANNEL);
        val logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(1, logoutRequests.size());
    }
}

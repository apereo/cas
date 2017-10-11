package org.apereo.cas.logout;

import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegisteredService.LogoutType;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.HttpMessage;
import org.apereo.cas.web.SimpleUrlValidatorFactoryBean;
import org.apereo.cas.web.UrlValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Jerome Leleu
 * @since 4.0.0
 */
@RunWith(JUnit4.class)
public class DefaultLogoutManagerTests {

    private static final String ID = "id";
    private static final String URL = "http://www.github.com";

    private DefaultLogoutManager logoutManager;

    @Mock
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

    @Before
    public void setUp() throws Exception {
        when(client.isValidEndPoint(any(String.class))).thenReturn(true);
        when(client.isValidEndPoint(any(URL.class))).thenReturn(true);
        when(client.sendMessageToEndPoint(any(HttpMessage.class))).thenReturn(true);

        final UrlValidator validator = new SimpleUrlValidatorFactoryBean(true).getObject();

        singleLogoutServiceMessageHandler = new DefaultSingleLogoutServiceMessageHandler(client, 
                new SamlCompliantLogoutMessageCreator(), servicesManager,
                new DefaultSingleLogoutServiceLogoutUrlBuilder(validator), true,
                new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));

        final Map<String, Service> services = new HashMap<>();
        this.simpleWebApplicationServiceImpl = RegisteredServiceTestUtils.getService(URL);
        services.put(ID, this.simpleWebApplicationServiceImpl);
        when(this.tgt.getServices()).thenReturn(services);

        this.logoutManager = new DefaultLogoutManager(new SamlCompliantLogoutMessageCreator(), 
                singleLogoutServiceMessageHandler, false, mock(LogoutExecutionPlan.class));
        this.registeredService = RegisteredServiceTestUtils.getRegisteredService(URL);
        when(servicesManager.findServiceBy(this.simpleWebApplicationServiceImpl)).thenReturn(this.registeredService);
    }

    @Test
    public void verifyServiceLogoutUrlIsUsed() throws Exception {
        this.registeredService.setLogoutUrl(new URL("https://www.apereo.org"));
        final Collection<LogoutRequest> logoutRequests = this.logoutManager.performLogout(tgt);
        final LogoutRequest logoutRequest = logoutRequests.iterator().next();
        assertEquals(logoutRequest.getLogoutUrl(), this.registeredService.getLogoutUrl());
    }

    @Test
    public void verifyLogoutDisabled() {
        this.logoutManager = new DefaultLogoutManager(new SamlCompliantLogoutMessageCreator(), 
                singleLogoutServiceMessageHandler, true, mock(LogoutExecutionPlan.class));

        final Collection<LogoutRequest> logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(0, logoutRequests.size());
    }

    @Test
    public void verifyLogoutAlreadyLoggedOut() {
        this.simpleWebApplicationServiceImpl.setLoggedOutAlready(true);
        final Collection<LogoutRequest> logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(0, logoutRequests.size());
    }

    @Test
    public void verifyLogoutTypeNotSet() {
        final Collection<LogoutRequest> logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(1, logoutRequests.size());
        final LogoutRequest logoutRequest = logoutRequests.iterator().next();
        assertEquals(ID, logoutRequest.getTicketId());
        assertEquals(this.simpleWebApplicationServiceImpl, logoutRequest.getService());
        assertEquals(LogoutRequestStatus.SUCCESS, logoutRequest.getStatus());
    }

    @Test
    public void verifyLogoutTypeBack() {
        this.registeredService.setLogoutType(LogoutType.BACK_CHANNEL);
        final Collection<LogoutRequest> logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(1, logoutRequests.size());
        final LogoutRequest logoutRequest = logoutRequests.iterator().next();
        assertEquals(ID, logoutRequest.getTicketId());
        assertEquals(this.simpleWebApplicationServiceImpl, logoutRequest.getService());
        assertEquals(LogoutRequestStatus.SUCCESS, logoutRequest.getStatus());
    }

    @Test
    public void verifyLogoutTypeNone() {
        this.registeredService.setLogoutType(LogoutType.NONE);
        final Collection<LogoutRequest> logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(0, logoutRequests.size());
    }

    @Test
    public void verifyLogoutTypeNull() {
        this.registeredService.setLogoutType(null);
        final Collection<LogoutRequest> logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(1, logoutRequests.size());
        final LogoutRequest logoutRequest = logoutRequests.iterator().next();
        assertEquals(ID, logoutRequest.getTicketId());
    }

    @Test
    public void verifyLogoutTypeFront() {
        this.registeredService.setLogoutType(LogoutType.FRONT_CHANNEL);
        final Collection<LogoutRequest> logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(1, logoutRequests.size());
        final LogoutRequest logoutRequest = logoutRequests.iterator().next();
        assertEquals(ID, logoutRequest.getTicketId());
        assertEquals(this.simpleWebApplicationServiceImpl, logoutRequest.getService());
        assertEquals(LogoutRequestStatus.NOT_ATTEMPTED, logoutRequest.getStatus());
    }
    
    @Test
    public void verifyAsynchronousLogout() {
        this.registeredService.setLogoutType(LogoutType.BACK_CHANNEL);
        final Collection<LogoutRequest> logoutRequests = this.logoutManager.performLogout(tgt);
        assertEquals(1, logoutRequests.size());
    }
}

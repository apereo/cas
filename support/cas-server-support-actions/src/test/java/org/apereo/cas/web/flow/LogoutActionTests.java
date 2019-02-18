package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.configuration.model.core.logout.LogoutProperties;
import org.apereo.cas.logout.DefaultSingleLogoutRequest;
import org.apereo.cas.logout.LogoutRequestStatus;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.flow.logout.LogoutAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.Cookie;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class LogoutActionTests extends AbstractWebflowActionsTests {

    private static final String COOKIE_TGC_ID = "CASTGC";
    private static final String TEST_SERVICE_ID = "TestService";

    private LogoutAction logoutAction;

    private DefaultServicesManager serviceManager;

    private MockHttpServletRequest request;

    private RequestContext requestContext;

    @BeforeEach
    public void onSetUp() {
        this.request = new MockHttpServletRequest();
        this.requestContext = mock(RequestContext.class);
        val servletExternalContext = mock(ServletExternalContext.class);
        when(this.requestContext.getExternalContext()).thenReturn(servletExternalContext);
        when(servletExternalContext.getNativeRequest()).thenReturn(request);
        when(servletExternalContext.getNativeResponse()).thenReturn(new MockHttpServletResponse());
        when(this.requestContext.getFlowScope()).thenReturn(new LocalAttributeMap());

        val publisher = mock(ApplicationEventPublisher.class);
        this.serviceManager = new DefaultServicesManager(new InMemoryServiceRegistry(publisher), publisher, new HashSet<>());
        this.serviceManager.load();
    }

    @Test
    public void verifyLogoutNoCookie() {
        val properties = new LogoutProperties();
        this.logoutAction = new LogoutAction(getWebApplicationServiceFactory(), this.serviceManager, properties);
        val event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
    }

    @Test
    public void verifyLogoutForServiceWithFollowRedirectsAndMatchingService() {
        this.request.addParameter("service", TEST_SERVICE_ID);
        val impl = new RegexRegisteredService();
        impl.setServiceId(TEST_SERVICE_ID);
        impl.setName(TEST_SERVICE_ID);
        this.serviceManager.save(impl);
        val properties = new LogoutProperties();
        properties.setFollowServiceRedirects(true);
        this.logoutAction = new LogoutAction(getWebApplicationServiceFactory(), this.serviceManager, properties);
        val event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
        assertEquals(TEST_SERVICE_ID, this.requestContext.getFlowScope().get("logoutRedirectUrl"));
    }

    @Test
    public void logoutForServiceWithNoFollowRedirects() {
        this.request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, TEST_SERVICE_ID);
        val properties = new LogoutProperties();
        this.logoutAction = new LogoutAction(getWebApplicationServiceFactory(), this.serviceManager, properties);
        val event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
        assertNull(this.requestContext.getFlowScope().get("logoutRedirectUrl"));
    }

    @Test
    public void logoutForServiceWithFollowRedirectsNoAllowedService() {
        this.request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, TEST_SERVICE_ID);
        val impl = new RegexRegisteredService();
        impl.setServiceId("http://FooBar");
        impl.setName("FooBar");
        this.serviceManager.save(impl);
        val properties = new LogoutProperties();
        this.logoutAction = new LogoutAction(getWebApplicationServiceFactory(), this.serviceManager, properties);
        val event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
        assertNull(this.requestContext.getFlowScope().get("logoutRedirectUrl"));
    }

    @Test
    public void verifyLogoutCookie() {
        val cookie = new Cookie(COOKIE_TGC_ID, "test");
        this.request.setCookies(cookie);
        val properties = new LogoutProperties();
        this.logoutAction = new LogoutAction(getWebApplicationServiceFactory(), this.serviceManager, properties);
        val event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
    }

    @Test
    public void verifyLogoutRequestBack() {
        val cookie = new Cookie(COOKIE_TGC_ID, "test");
        this.request.setCookies(cookie);
        val logoutRequest = DefaultSingleLogoutRequest.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService())
            .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
            .build();
        logoutRequest.setStatus(LogoutRequestStatus.SUCCESS);
        WebUtils.putLogoutRequests(this.requestContext, Collections.singletonList(logoutRequest));
        val properties = new LogoutProperties();
        this.logoutAction = new LogoutAction(getWebApplicationServiceFactory(), this.serviceManager, properties);
        val event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
    }

    @Test
    public void verifyLogoutRequestFront() {
        val cookie = new Cookie(COOKIE_TGC_ID, "test");
        this.request.setCookies(cookie);
        val logoutRequest = DefaultSingleLogoutRequest.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService())
            .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
            .build();
        WebUtils.putLogoutRequests(this.requestContext, Collections.singletonList(logoutRequest));
        val properties = new LogoutProperties();
        this.logoutAction = new LogoutAction(getWebApplicationServiceFactory(), this.serviceManager, properties);
        val event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_FRONT, event.getId());
        val logoutRequests = WebUtils.getLogoutRequests(this.requestContext);
        assertEquals(1, logoutRequests.size());
        assertEquals(logoutRequest, logoutRequests.get(0));
    }
}

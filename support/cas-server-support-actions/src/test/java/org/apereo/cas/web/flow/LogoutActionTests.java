package org.apereo.cas.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.logout.DefaultLogoutRequest;
import org.apereo.cas.logout.LogoutRequest;
import org.apereo.cas.logout.LogoutRequestStatus;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.Cookie;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class LogoutActionTests extends AbstractCentralAuthenticationServiceTests {

    private static final String COOKIE_TGC_ID = "CASTGC";

    private LogoutAction logoutAction;
    
    private InMemoryServiceRegistry serviceRegistryDao;

    private DefaultServicesManager serviceManager;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    private RequestContext requestContext;

    @Before
    public void onSetUp() throws Exception {
        this.request = new MockHttpServletRequest();
        this.response = new MockHttpServletResponse();
        this.requestContext = mock(RequestContext.class);
        final ServletExternalContext servletExternalContext = mock(ServletExternalContext.class);
        when(this.requestContext.getExternalContext()).thenReturn(servletExternalContext);
        when(servletExternalContext.getNativeRequest()).thenReturn(request);
        when(servletExternalContext.getNativeResponse()).thenReturn(response);
        final LocalAttributeMap flowScope = new LocalAttributeMap();
        when(this.requestContext.getFlowScope()).thenReturn(flowScope);

        this.serviceRegistryDao = new InMemoryServiceRegistry();
        this.serviceManager = new DefaultServicesManager(serviceRegistryDao);
        this.serviceManager.load();

        this.logoutAction = new LogoutAction(getWebApplicationServiceFactory(), this.serviceManager, false);
    }

    @Test
    public void verifyLogoutNoCookie() throws Exception {
        final Event event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(LogoutAction.FINISH_EVENT, event.getId());
    }

    @Test
    public void verifyLogoutForServiceWithFollowRedirectsAndMatchingService() throws Exception {
        this.request.addParameter("service", "TestService");
        final RegexRegisteredService impl = new RegexRegisteredService();
        impl.setServiceId("TestService");
        impl.setName("TestService");
        this.serviceManager.save(impl);
        this.logoutAction.setFollowServiceRedirects(true);
        final Event event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(LogoutAction.FINISH_EVENT, event.getId());
        assertEquals("TestService", this.requestContext.getFlowScope().get("logoutRedirectUrl"));
    }

    @Test
    public void logoutForServiceWithNoFollowRedirects() throws Exception {
        this.request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "TestService");
        this.logoutAction.setFollowServiceRedirects(false);
        final Event event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(LogoutAction.FINISH_EVENT, event.getId());
        assertNull(this.requestContext.getFlowScope().get("logoutRedirectUrl"));
    }

    @Test
    public void logoutForServiceWithFollowRedirectsNoAllowedService() throws Exception {
        this.request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "TestService");
        final RegexRegisteredService impl = new RegexRegisteredService();
        impl.setServiceId("http://FooBar");
        impl.setName("FooBar");
        this.serviceManager.save(impl);
        this.logoutAction.setFollowServiceRedirects(true);
        final Event event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(LogoutAction.FINISH_EVENT, event.getId());
        assertNull(this.requestContext.getFlowScope().get("logoutRedirectUrl"));
    }

    @Test
    public void verifyLogoutCookie() throws Exception {
        final Cookie cookie = new Cookie(COOKIE_TGC_ID, "test");
        this.request.setCookies(cookie);
        final Event event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(LogoutAction.FINISH_EVENT, event.getId());
    }

    @Test
    public void verifyLogoutRequestBack() throws Exception {
        final Cookie cookie = new Cookie(COOKIE_TGC_ID, "test");
        this.request.setCookies(cookie);
        final LogoutRequest logoutRequest = new DefaultLogoutRequest(StringUtils.EMPTY, null, null);
        logoutRequest.setStatus(LogoutRequestStatus.SUCCESS);
        WebUtils.putLogoutRequests(this.requestContext, Arrays.asList(logoutRequest));
        final Event event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(LogoutAction.FINISH_EVENT, event.getId());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void verifyLogoutRequestFront() throws Exception {
        final Cookie cookie = new Cookie(COOKIE_TGC_ID, "test");
        this.request.setCookies(cookie);
        final LogoutRequest logoutRequest = new DefaultLogoutRequest(StringUtils.EMPTY, null, null);
        WebUtils.putLogoutRequests(this.requestContext, Arrays.asList(logoutRequest));
        final Event event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(LogoutAction.FRONT_EVENT, event.getId());
        final List<LogoutRequest> logoutRequests = WebUtils.getLogoutRequests(this.requestContext);
        assertEquals(1, logoutRequests.size());
        assertEquals(logoutRequest, logoutRequests.get(0));
    }
}

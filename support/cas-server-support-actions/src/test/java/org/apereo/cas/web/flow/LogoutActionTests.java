package org.apereo.cas.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.configuration.model.core.logout.LogoutProperties;
import org.apereo.cas.logout.DefaultLogoutRequest;
import org.apereo.cas.logout.LogoutRequest;
import org.apereo.cas.logout.LogoutRequestStatus;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;
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
    private static final String TEST_SERVICE_ID = "TestService";

    private LogoutAction logoutAction;

    private DefaultServicesManager serviceManager;

    private MockHttpServletRequest request;

    private RequestContext requestContext;

    @Before
    public void onSetUp() {
        this.request = new MockHttpServletRequest();
        this.requestContext = mock(RequestContext.class);
        final ServletExternalContext servletExternalContext = mock(ServletExternalContext.class);
        when(this.requestContext.getExternalContext()).thenReturn(servletExternalContext);
        when(servletExternalContext.getNativeRequest()).thenReturn(request);
        when(servletExternalContext.getNativeResponse()).thenReturn(new MockHttpServletResponse());
        when(this.requestContext.getFlowScope()).thenReturn(new LocalAttributeMap());

        this.serviceManager = new DefaultServicesManager(new InMemoryServiceRegistry(), mock(ApplicationEventPublisher.class));
        this.serviceManager.load();
    }

    @Test
    public void verifyLogoutNoCookie() throws Exception {
        final LogoutProperties properties = new LogoutProperties();
        this.logoutAction = new LogoutAction(getWebApplicationServiceFactory(), this.serviceManager, properties);
        final Event event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(LogoutAction.FINISH_EVENT, event.getId());
    }

    @Test
    public void verifyLogoutForServiceWithFollowRedirectsAndMatchingService() throws Exception {
        this.request.addParameter("service", TEST_SERVICE_ID);
        final RegexRegisteredService impl = new RegexRegisteredService();
        impl.setServiceId(TEST_SERVICE_ID);
        impl.setName(TEST_SERVICE_ID);
        this.serviceManager.save(impl);
        final LogoutProperties properties = new LogoutProperties();
        properties.setFollowServiceRedirects(true);
        this.logoutAction = new LogoutAction(getWebApplicationServiceFactory(), this.serviceManager, properties);
        final Event event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(LogoutAction.FINISH_EVENT, event.getId());
        assertEquals(TEST_SERVICE_ID, this.requestContext.getFlowScope().get("logoutRedirectUrl"));
    }

    @Test
    public void logoutForServiceWithNoFollowRedirects() throws Exception {
        this.request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, TEST_SERVICE_ID);
        final LogoutProperties properties = new LogoutProperties();
        this.logoutAction = new LogoutAction(getWebApplicationServiceFactory(), this.serviceManager, properties);
        final Event event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(LogoutAction.FINISH_EVENT, event.getId());
        assertNull(this.requestContext.getFlowScope().get("logoutRedirectUrl"));
    }

    @Test
    public void logoutForServiceWithFollowRedirectsNoAllowedService() throws Exception {
        this.request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, TEST_SERVICE_ID);
        final RegexRegisteredService impl = new RegexRegisteredService();
        impl.setServiceId("http://FooBar");
        impl.setName("FooBar");
        this.serviceManager.save(impl);
        final LogoutProperties properties = new LogoutProperties();
        this.logoutAction = new LogoutAction(getWebApplicationServiceFactory(), this.serviceManager, properties);
        final Event event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(LogoutAction.FINISH_EVENT, event.getId());
        assertNull(this.requestContext.getFlowScope().get("logoutRedirectUrl"));
    }

    @Test
    public void verifyLogoutCookie() throws Exception {
        final Cookie cookie = new Cookie(COOKIE_TGC_ID, "test");
        this.request.setCookies(cookie);
        final LogoutProperties properties = new LogoutProperties();
        this.logoutAction = new LogoutAction(getWebApplicationServiceFactory(), this.serviceManager, properties);
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
        final LogoutProperties properties = new LogoutProperties();
        this.logoutAction = new LogoutAction(getWebApplicationServiceFactory(), this.serviceManager, properties);
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
        final LogoutProperties properties = new LogoutProperties();
        this.logoutAction = new LogoutAction(getWebApplicationServiceFactory(), this.serviceManager, properties);
        final Event event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(LogoutAction.FRONT_EVENT, event.getId());
        final List<LogoutRequest> logoutRequests = WebUtils.getLogoutRequests(this.requestContext);
        assertEquals(1, logoutRequests.size());
        assertEquals(logoutRequest, logoutRequests.get(0));
    }
}

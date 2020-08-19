package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.configuration.model.core.logout.LogoutProperties;
import org.apereo.cas.logout.DefaultLogoutRedirectionStrategy;
import org.apereo.cas.logout.DefaultSingleLogoutRequestContext;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.logout.LogoutRequestStatus;
import org.apereo.cas.logout.SingleLogoutExecutionRequest;
import org.apereo.cas.logout.slo.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import org.apereo.cas.web.SimpleUrlValidator;
import org.apereo.cas.web.flow.logout.LogoutAction;
import org.apereo.cas.web.support.WebUtils;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import javax.servlet.http.Cookie;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("WebflowActions")
public class LogoutActionTests extends AbstractWebflowActionsTests {

    private static final String COOKIE_TGC_ID = "CASTGC";

    private static final String TEST_SERVICE_ID = "TestService";

    private LogoutAction logoutAction;

    private DefaultServicesManager serviceManager;

    private MockHttpServletRequest request;

    private MockRequestContext requestContext;

    @BeforeEach
    public void onSetUp() {
        this.request = new MockHttpServletRequest();
        this.requestContext = new MockRequestContext();
        val response = new MockHttpServletResponse();
        requestContext.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(requestContext);
        ExternalContextHolder.setExternalContext(requestContext.getExternalContext());

        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val context = ServicesManagerConfigurationContext.builder()
            .serviceRegistry(new InMemoryServiceRegistry(appCtx))
            .applicationContext(appCtx)
            .environments(new HashSet<>(0))
            .servicesCache(Caffeine.newBuilder().build())
            .build();
        this.serviceManager = new DefaultServicesManager(context);
        this.serviceManager.load();
    }

    @Test
    public void verifyLogoutNoCookie() {
        val properties = new LogoutProperties();
        this.logoutAction = getLogoutAction(properties);
        val event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
    }

    @Test
    public void verifyLogoutForServiceWithFollowRedirectsAndMatchingService() {
        this.request.addParameter("service", TEST_SERVICE_ID);
        val service = new RegexRegisteredService();
        service.setServiceId(TEST_SERVICE_ID);
        service.setName(TEST_SERVICE_ID);
        this.serviceManager.save(service);
        val properties = new LogoutProperties();
        properties.setFollowServiceRedirects(true);
        this.logoutAction = getLogoutAction(properties);
        val event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
        assertEquals(TEST_SERVICE_ID, WebUtils.getLogoutRedirectUrl(this.requestContext, String.class));
    }

    @Test
    public void verifyLogoutForServiceWithFollowRedirectsAndInternalService() {
        val service = new RegexRegisteredService();
        service.setServiceId(TEST_SERVICE_ID);
        service.setName(TEST_SERVICE_ID);
        this.serviceManager.save(service);
        val properties = new LogoutProperties();
        properties.setFollowServiceRedirects(true);
        this.logoutAction = getLogoutAction(properties);
        WebUtils.putLogoutRedirectUrl(request, "https://example.com");
        val event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
        assertEquals("https://example.com", WebUtils.getLogoutRedirectUrl(this.requestContext, String.class));
    }

    @Test
    public void logoutForServiceWithNoFollowRedirects() {
        this.request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, TEST_SERVICE_ID);
        val properties = new LogoutProperties();
        this.logoutAction = getLogoutAction(properties);
        val event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
        assertNull(WebUtils.getLogoutRedirectUrl(this.requestContext, String.class));
    }

    @Test
    public void logoutForServiceWithFollowRedirectsNoAllowedService() {
        this.request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, TEST_SERVICE_ID);
        val service = new RegexRegisteredService();
        service.setServiceId("http://FooBar");
        service.setName("FooBar");
        this.serviceManager.save(service);
        val properties = new LogoutProperties();
        this.logoutAction = getLogoutAction(properties);
        val event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
        assertNull(WebUtils.getLogoutRedirectUrl(this.requestContext, String.class));
    }

    @Test
    public void verifyLogoutCookie() {
        val cookie = new Cookie(COOKIE_TGC_ID, "test");
        this.request.setCookies(cookie);
        val properties = new LogoutProperties();
        this.logoutAction = getLogoutAction(properties);
        val event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
    }

    @Test
    public void verifyLogoutRequestBack() {
        val cookie = new Cookie(COOKIE_TGC_ID, "test");
        this.request.setCookies(cookie);
        val logoutRequest = DefaultSingleLogoutRequestContext.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService())
            .executionRequest(SingleLogoutExecutionRequest.builder()
                .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
                .build())
            .build();
        logoutRequest.setStatus(LogoutRequestStatus.SUCCESS);
        WebUtils.putLogoutRequests(this.requestContext, List.of(logoutRequest));
        val properties = new LogoutProperties();
        this.logoutAction = getLogoutAction(properties);
        val event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
    }

    @Test
    public void verifyLogoutRequestFront() {
        val cookie = new Cookie(COOKIE_TGC_ID, "test");
        this.request.setCookies(cookie);
        val logoutRequest = DefaultSingleLogoutRequestContext.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService())
            .executionRequest(SingleLogoutExecutionRequest.builder()
                .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
                .build())
            .build();
        WebUtils.putLogoutRequests(this.requestContext, List.of(logoutRequest));
        val properties = new LogoutProperties();
        this.logoutAction = getLogoutAction(properties);
        val event = this.logoutAction.doExecute(this.requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_FRONT, event.getId());
        val logoutRequests = WebUtils.getLogoutRequests(this.requestContext);
        assertEquals(1, logoutRequests.size());
        assertEquals(logoutRequest, logoutRequests.get(0));
    }

    private LogoutAction getLogoutAction(final LogoutProperties properties) {
        val plan = mock(LogoutExecutionPlan.class);
        when(plan.getLogoutRedirectionStrategies()).thenReturn(List.of(new DefaultLogoutRedirectionStrategy(getWebApplicationServiceFactory(), properties,
            new DefaultSingleLogoutServiceLogoutUrlBuilder(serviceManager, SimpleUrlValidator.getInstance()))));
        return new LogoutAction(plan);
    }
}

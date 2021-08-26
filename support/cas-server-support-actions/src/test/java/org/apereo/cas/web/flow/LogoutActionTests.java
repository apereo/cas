package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.logout.DefaultSingleLogoutRequestContext;
import org.apereo.cas.logout.LogoutRequestStatus;
import org.apereo.cas.logout.SingleLogoutExecutionRequest;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import javax.servlet.http.Cookie;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("WebflowActions")
public class LogoutActionTests {

    private static final String COOKIE_TGC_ID = "CASTGC";

    private static final String TEST_SERVICE_ID = "TestService";

    private MockHttpServletRequest request;

    private MockRequestContext requestContext;

    @BeforeEach
    public void onSetUp() {
        request = new MockHttpServletRequest();
        requestContext = new MockRequestContext();
        val response = new MockHttpServletResponse();
        requestContext.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(requestContext);
        ExternalContextHolder.setExternalContext(requestContext.getExternalContext());
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = "cas.logout.follow-service-redirects=true")
    public class FollowServiceRedirectsTests extends AbstractWebflowActionsTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_LOGOUT)
        private Action logoutAction;

        @Test
        public void verifyLogoutForServiceWithFollowRedirectsAndMatchingService() throws Exception {
            request.addParameter("service", TEST_SERVICE_ID);
            val service = new RegexRegisteredService();
            service.setServiceId(TEST_SERVICE_ID);
            service.setName(TEST_SERVICE_ID);
            getServicesManager().save(service);
            val event = logoutAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
            assertEquals(TEST_SERVICE_ID, WebUtils.getLogoutRedirectUrl(requestContext, String.class));
        }

        @Test
        public void verifyLogoutForServiceWithFollowRedirectsAndInternalService() throws Exception {
            val service = new RegexRegisteredService();
            service.setServiceId(TEST_SERVICE_ID);
            service.setName(TEST_SERVICE_ID);
            getServicesManager().save(service);

            WebUtils.putLogoutRedirectUrl(request, "https://example.com");
            val event = logoutAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
            assertEquals("https://example.com", WebUtils.getLogoutRedirectUrl(requestContext, String.class));
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = "cas.logout.follow-service-redirects=true")
    public class DefaultTests extends AbstractWebflowActionsTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_LOGOUT)
        private Action logoutAction;

        @Test
        public void verifyLogoutNoCookie() throws Exception {
            val event = logoutAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
        }

        @Test
        public void logoutForServiceWithNoFollowRedirects() throws Exception {
            getServicesManager().deleteAll();
            request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, TEST_SERVICE_ID);
            val event = logoutAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
            assertNull(WebUtils.getLogoutRedirectUrl(requestContext, String.class));
        }

        @Test
        public void logoutForServiceWithFollowRedirectsNoAllowedService() throws Exception {
            getServicesManager().deleteAll();
            request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, TEST_SERVICE_ID);
            val service = new RegexRegisteredService();
            service.setServiceId("http://FooBar");
            service.setName("FooBar");
            getServicesManager().save(service);
            val event = logoutAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
            assertNull(WebUtils.getLogoutRedirectUrl(requestContext, String.class));
        }

        @Test
        public void verifyLogoutCookie() throws Exception {
            val cookie = new Cookie(COOKIE_TGC_ID, "test");
            request.setCookies(cookie);
            val event = logoutAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
        }

        @Test
        public void verifyLogoutRequestBack() throws Exception {
            val cookie = new Cookie(COOKIE_TGC_ID, "test");
            request.setCookies(cookie);
            val logoutRequest = DefaultSingleLogoutRequestContext.builder()
                .registeredService(RegisteredServiceTestUtils.getRegisteredService())
                .executionRequest(SingleLogoutExecutionRequest.builder()
                    .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
                    .build())
                .build();
            logoutRequest.setStatus(LogoutRequestStatus.SUCCESS);
            WebUtils.putLogoutRequests(requestContext, List.of(logoutRequest));
            val event = logoutAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
        }

        @Test
        public void verifyLogoutRequestFront() throws Exception {
            val cookie = new Cookie(COOKIE_TGC_ID, "test");
            request.setCookies(cookie);
            val logoutRequest = DefaultSingleLogoutRequestContext.builder()
                .registeredService(RegisteredServiceTestUtils.getRegisteredService())
                .executionRequest(SingleLogoutExecutionRequest.builder()
                    .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
                    .build())
                .build();
            WebUtils.putLogoutRequests(requestContext, List.of(logoutRequest));
            val event = logoutAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_FRONT, event.getId());
            val logoutRequests = WebUtils.getLogoutRequests(requestContext);
            assertEquals(1, logoutRequests.size());
            assertEquals(logoutRequest, logoutRequests.get(0));
        }
    }


}

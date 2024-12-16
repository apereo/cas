package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.logout.DefaultSingleLogoutRequestContext;
import org.apereo.cas.logout.LogoutRequestStatus;
import org.apereo.cas.logout.slo.SingleLogoutExecutionRequest;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("WebflowActions")
class LogoutActionTests {

    private static final String COOKIE_TGC_ID = "CASTGC";

    @Nested
    @TestPropertySource(properties = "cas.logout.follow-service-redirects=true")
    class FollowServiceRedirectsTests extends AbstractWebflowActionsTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_LOGOUT)
        private Action logoutAction;

        @Test
        void verifyLogoutForServiceWithFollowRedirectsAndMatchingService() throws Throwable {
            val testServiceId = UUID.randomUUID().toString();
            val requestContext = MockRequestContext.create(applicationContext);
            requestContext.getHttpServletRequest().addParameter(CasProtocolConstants.PARAMETER_SERVICE, testServiceId);
            val service = new CasRegisteredService();
            service.setServiceId(testServiceId);
            service.setName(testServiceId);
            getServicesManager().save(service);
            val event = logoutAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
            assertEquals(testServiceId, WebUtils.getLogoutRedirectUrl(requestContext, String.class));
        }

        @Test
        void verifyLogoutForServiceWithFollowRedirectsAndInternalService() throws Throwable {
            val testServiceId = UUID.randomUUID().toString();
            val requestContext = MockRequestContext.create(applicationContext);
            val service = new CasRegisteredService();
            service.setServiceId(testServiceId);
            service.setName(testServiceId);
            getServicesManager().save(service);

            WebUtils.putLogoutRedirectUrl(requestContext.getHttpServletRequest(), "https://example.com");
            val event = logoutAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
            assertEquals("https://example.com", WebUtils.getLogoutRedirectUrl(requestContext, String.class));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.logout.follow-service-redirects=true")
    class DefaultTests extends AbstractWebflowActionsTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_LOGOUT)
        private Action logoutAction;

        @Test
        void verifyLogoutNoCookie() throws Throwable {
            val requestContext = MockRequestContext.create(applicationContext);
            val event = logoutAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
        }

        @Test
        void logoutForServiceWithNoFollowRedirects() throws Exception {
            val testServiceId = UUID.randomUUID().toString();
            val requestContext = MockRequestContext.create(applicationContext);
            requestContext.getHttpServletRequest().addParameter(CasProtocolConstants.PARAMETER_SERVICE, testServiceId);
            val event = logoutAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
            assertNull(WebUtils.getLogoutRedirectUrl(requestContext, String.class));
        }

        @Test
        void logoutForServiceWithFollowRedirectsNoAllowedService() throws Exception {
            val requestContext = MockRequestContext.create(applicationContext);
            requestContext.getHttpServletRequest().addParameter(CasProtocolConstants.PARAMETER_SERVICE, UUID.randomUUID().toString());
            val event = logoutAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
            assertNull(WebUtils.getLogoutRedirectUrl(requestContext, String.class));
        }

        @Test
        void verifyLogoutCookie() throws Throwable {
            val requestContext = MockRequestContext.create(applicationContext);
            val cookie = new Cookie(COOKIE_TGC_ID, "test");
            requestContext.setHttpRequestCookies(cookie);
            val event = logoutAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
        }

        @Test
        void verifyLogoutRequestBack() throws Throwable {
            val requestContext = MockRequestContext.create(applicationContext);
            val cookie = new Cookie(COOKIE_TGC_ID, "test");
            requestContext.setHttpRequestCookies(cookie);
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
        void verifyLogoutRequestFront() throws Throwable {
            val requestContext = MockRequestContext.create(applicationContext);
            val cookie = new Cookie(COOKIE_TGC_ID, "test");
            requestContext.setHttpRequestCookies(cookie);
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
            assertEquals(logoutRequest, logoutRequests.getFirst());
        }
    }


}

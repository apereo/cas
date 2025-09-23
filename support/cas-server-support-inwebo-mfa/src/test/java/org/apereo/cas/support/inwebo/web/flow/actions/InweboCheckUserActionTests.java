package org.apereo.cas.support.inwebo.web.flow.actions;

import org.apereo.cas.support.inwebo.service.response.InweboLoginSearchResponse;
import org.apereo.cas.support.inwebo.service.response.InweboResult;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import static org.apereo.cas.support.inwebo.web.flow.actions.InweboWebflowConstants.BROWSER_AUTHENTICATOR;
import static org.apereo.cas.support.inwebo.web.flow.actions.InweboWebflowConstants.MUST_ENROLL;
import static org.apereo.cas.support.inwebo.web.flow.actions.InweboWebflowConstants.PUSH;
import static org.apereo.cas.support.inwebo.web.flow.actions.InweboWebflowConstants.SELECT;
import static org.apereo.cas.support.inwebo.web.flow.actions.InweboWebflowConstants.VA;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests {@link InweboCheckUserAction}.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Tag("WebflowMfaActions")
class InweboCheckUserActionTests {

    private static final String SITE_ALIAS = "7845zesf357dsq89s74za6z4e5df";
    private static final int USER_ID = 123456;

    @TestPropertySource(properties = "cas.authn.mfa.inwebo.site-alias=" + SITE_ALIAS)
    abstract static class BaseTests extends BaseInweboActionTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_INWEBO_CHECK_USER)
        protected Action action;

        protected static InweboLoginSearchResponse loginSearchOk(final int activationStatus, final int userId) {
            val response = new InweboLoginSearchResponse();
            response.setResult(InweboResult.OK);
            response.setCount(1);
            response.setUserId(userId);
            response.setActivationStatus(activationStatus);
            return response;
        }
    }

    @Nested
    class DefaultTests extends BaseTests {
        @Test
        void verifyNoUser() throws Throwable {
            when(service.loginSearchQuery(LOGIN)).thenReturn(loginSearchOk(3, 0));

            val event = action.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
            val flowScope = requestContext.getFlowScope();
            assertFalse(flowScope.contains(MUST_ENROLL));
            assertEquals(VA, flowScope.get(BROWSER_AUTHENTICATOR));
        }

        @Test
        void verifyInweboException() throws Throwable {
            when(service.loginSearchQuery(LOGIN)).thenThrow(new RuntimeException());

            val event = action.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
            val flowScope = requestContext.getFlowScope();
            assertFalse(flowScope.contains(MUST_ENROLL));
            assertEquals(VA, flowScope.get(BROWSER_AUTHENTICATOR));
        }

        @Test
        void verifyUserBlocked() throws Throwable {
            val loginSearch = loginSearchOk(3, USER_ID);
            loginSearch.setUserStatus(1);
            when(service.loginSearchQuery(LOGIN)).thenReturn(loginSearch);

            val event = action.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
            val flowScope = requestContext.getFlowScope();
            assertFalse(flowScope.contains(MUST_ENROLL));
            assertEquals(VA, flowScope.get(BROWSER_AUTHENTICATOR));
        }

        @Test
        void verifyUserNotRegisteredVA() throws Throwable {
            when(service.loginSearchQuery(LOGIN)).thenReturn(loginSearchOk(0, USER_ID));

            val event = action.execute(requestContext);
            assertEquals(VA, event.getId());
            val flowScope = requestContext.getFlowScope();
            assertFalse(flowScope.contains(MUST_ENROLL));
            assertEquals(VA, flowScope.get(BROWSER_AUTHENTICATOR));
        }

        @Test
        void verifyPush() throws Throwable {
            when(service.loginSearchQuery(LOGIN)).thenReturn(loginSearchOk(1, USER_ID));

            val event = action.execute(requestContext);
            assertEquals(PUSH, event.getId());
            val flowScope = requestContext.getFlowScope();
            assertFalse(flowScope.contains(MUST_ENROLL));
            assertEquals(VA, flowScope.get(BROWSER_AUTHENTICATOR));
        }

        @Test
        void verifyUnexpectedStatus2() throws Throwable {
            when(service.loginSearchQuery(LOGIN)).thenReturn(loginSearchOk(2, USER_ID));

            val event = action.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
            val flowScope = requestContext.getFlowScope();
            assertFalse(flowScope.contains(MUST_ENROLL));
            assertEquals(VA, flowScope.get(BROWSER_AUTHENTICATOR));
        }

        @Test
        void verifyUnexpectedStatus3() throws Throwable {
            when(service.loginSearchQuery(LOGIN)).thenReturn(loginSearchOk(3, USER_ID));

            val event = action.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
            val flowScope = requestContext.getFlowScope();
            assertFalse(flowScope.contains(MUST_ENROLL));
            assertEquals(VA, flowScope.get(BROWSER_AUTHENTICATOR));
        }

        @Test
        void verifyBrowserVA() throws Throwable {
            when(service.loginSearchQuery(LOGIN)).thenReturn(loginSearchOk(4, USER_ID));

            val event = action.execute(requestContext);
            assertEquals(VA, event.getId());
            val flowScope = requestContext.getFlowScope();
            assertFalse(flowScope.contains(MUST_ENROLL));
            assertEquals(VA, flowScope.get(BROWSER_AUTHENTICATOR));
        }

        @Test
        void verifyPushAndBrowserVA() throws Throwable {
            when(service.loginSearchQuery(LOGIN)).thenReturn(loginSearchOk(5, USER_ID));

            val event = action.execute(requestContext);
            assertEquals(SELECT, event.getId());
            val flowScope = requestContext.getFlowScope();
            assertFalse(flowScope.contains(MUST_ENROLL));
            assertEquals(VA, flowScope.get(BROWSER_AUTHENTICATOR));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.mfa.inwebo.browser-authenticator=M_ACCESS_WEB")
    class MAAuthenticatorTests extends BaseTests {
        @Test
        void verifyPushAndBrowserMA() throws Throwable {
            when(service.loginSearchQuery(LOGIN)).thenReturn(loginSearchOk(5, USER_ID));

            val event = action.execute(requestContext);
            assertEquals(SELECT, event.getId());
            val flowScope = requestContext.getFlowScope();
            assertFalse(flowScope.contains(MUST_ENROLL));
            assertEquals(InweboWebflowConstants.MA, flowScope.get(BROWSER_AUTHENTICATOR));
        }

        @Test
        void verifyBrowserMA() throws Throwable {
            when(service.loginSearchQuery(LOGIN)).thenReturn(loginSearchOk(4, USER_ID));

            val event = action.execute(requestContext);
            assertEquals(InweboWebflowConstants.MA, event.getId());
            val flowScope = requestContext.getFlowScope();
            assertFalse(flowScope.contains(MUST_ENROLL));
            assertEquals(InweboWebflowConstants.MA, flowScope.get(BROWSER_AUTHENTICATOR));
        }

        @Test
        void verifyUserNotRegisteredMA() throws Throwable {
            when(service.loginSearchQuery(LOGIN)).thenReturn(loginSearchOk(0, USER_ID));

            val event = action.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
            val flowScope = requestContext.getFlowScope();
            assertTrue((Boolean) flowScope.get(MUST_ENROLL));
            assertEquals(InweboWebflowConstants.MA, flowScope.get(BROWSER_AUTHENTICATOR));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.mfa.inwebo.browser-authenticator=NONE")
    class NoneAuthenticatorTests extends BaseTests {
        @Test
        void verifyPushAndBrowserNone() throws Throwable {
            when(service.loginSearchQuery(LOGIN)).thenReturn(loginSearchOk(5, USER_ID));

            val event = action.execute(requestContext);
            assertEquals(PUSH, event.getId());
            val flowScope = requestContext.getFlowScope();
            assertFalse(flowScope.contains(MUST_ENROLL));
            assertNull(flowScope.get(BROWSER_AUTHENTICATOR));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.mfa.inwebo.push-enabled=false")
    class VAWithoutPushTests extends BaseTests {
        @Test
        void verifyPushDisabledAndBrowserVA() throws Throwable {
            when(service.loginSearchQuery(LOGIN)).thenReturn(loginSearchOk(5, USER_ID));
            val event = action.execute(requestContext);
            assertEquals(VA, event.getId());
            val flowScope = requestContext.getFlowScope();
            assertFalse(flowScope.contains(MUST_ENROLL));
            assertEquals(VA, flowScope.get(BROWSER_AUTHENTICATOR));
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.mfa.inwebo.push-enabled=false",
        "cas.authn.mfa.inwebo.browser-authenticator=M_ACCESS_WEB"
    })
    class MAWithoutPushTests extends BaseTests {
        @Test
        void verifyPushDisabledAndBrowserMA() throws Throwable {
            when(service.loginSearchQuery(LOGIN)).thenReturn(loginSearchOk(5, USER_ID));

            val event = action.execute(requestContext);
            assertEquals(InweboWebflowConstants.MA, event.getId());
            val flowScope = requestContext.getFlowScope();
            assertFalse(flowScope.contains(MUST_ENROLL));
            assertEquals(InweboWebflowConstants.MA, flowScope.get(BROWSER_AUTHENTICATOR));
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.mfa.inwebo.push-enabled=false",
        "cas.authn.mfa.inwebo.browser-authenticator=NONE"
    })
    class NoneWithoutPushTests extends BaseTests {
        @Test
        void verifyPushDisabledAndBrowserNone() throws Throwable {
            when(service.loginSearchQuery(LOGIN)).thenReturn(loginSearchOk(5, USER_ID));

            val event = action.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
            val flowScope = requestContext.getFlowScope();
            assertFalse(flowScope.contains(MUST_ENROLL));
            assertNull(flowScope.get(BROWSER_AUTHENTICATOR));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.mfa.inwebo.push-auto=false")
    class WithoutPushAutoTests extends BaseTests {
        @Test
        void verifyPushAuto() throws Throwable {
            when(service.loginSearchQuery(LOGIN)).thenReturn(loginSearchOk(1, USER_ID));
            val event = action.execute(requestContext);
            assertEquals(SELECT, event.getId());
            val flowScope = requestContext.getFlowScope();
            assertFalse(flowScope.contains(MUST_ENROLL));
            assertEquals(VA, flowScope.get(BROWSER_AUTHENTICATOR));
        }

    }

}

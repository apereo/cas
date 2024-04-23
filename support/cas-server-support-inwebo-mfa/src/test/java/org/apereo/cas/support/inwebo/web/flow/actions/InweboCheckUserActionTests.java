package org.apereo.cas.support.inwebo.web.flow.actions;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.InweboMultifactorAuthenticationProperties;
import org.apereo.cas.support.inwebo.service.response.InweboLoginSearchResponse;
import org.apereo.cas.support.inwebo.service.response.InweboResult;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.apereo.cas.support.inwebo.web.flow.actions.WebflowConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests {@link InweboCheckUserAction}.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Tag("WebflowMfaActions")
class InweboCheckUserActionTests extends BaseInweboActionTests {

    private static final String SITE_ALIAS = "7845zesf357dsq89s74za6z4e5df";
    private static final int USER_ID = 123456;

    private InweboMultifactorAuthenticationProperties inwebo;

    private InweboCheckUserAction action;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        inwebo = new InweboMultifactorAuthenticationProperties();
        inwebo.setSiteAlias(SITE_ALIAS);
        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().setInwebo(inwebo);
        action = new InweboCheckUserAction(service, casProperties);
    }

    private static InweboLoginSearchResponse loginSearchOk(final int activationStatus, final int userId) {
        val response = new InweboLoginSearchResponse();
        response.setResult(InweboResult.OK);
        response.setCount(1);
        response.setUserId(userId);
        response.setActivationStatus(activationStatus);
        return response;
    }

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
    void verifyUserNotRegisteredMA() throws Throwable {
        inwebo.setBrowserAuthenticator(InweboMultifactorAuthenticationProperties.BrowserAuthenticatorTypes.M_ACCESS_WEB);
        when(service.loginSearchQuery(LOGIN)).thenReturn(loginSearchOk(0, USER_ID));

        val event = action.execute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        val flowScope = requestContext.getFlowScope();
        assertTrue((Boolean) flowScope.get(MUST_ENROLL));
        assertEquals(MA, flowScope.get(BROWSER_AUTHENTICATOR));
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
    void verifyPushAuto() throws Throwable {
        inwebo.setPushAuto(false);
        when(service.loginSearchQuery(LOGIN)).thenReturn(loginSearchOk(1, USER_ID));

        val event = action.execute(requestContext);
        assertEquals(SELECT, event.getId());
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
    void verifyBrowserMA() throws Throwable {
        inwebo.setBrowserAuthenticator(InweboMultifactorAuthenticationProperties.BrowserAuthenticatorTypes.M_ACCESS_WEB);
        when(service.loginSearchQuery(LOGIN)).thenReturn(loginSearchOk(4, USER_ID));

        val event = action.execute(requestContext);
        assertEquals(MA, event.getId());
        val flowScope = requestContext.getFlowScope();
        assertFalse(flowScope.contains(MUST_ENROLL));
        assertEquals(MA, flowScope.get(BROWSER_AUTHENTICATOR));
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

    @Test
    void verifyPushAndBrowserMA() throws Throwable {
        inwebo.setBrowserAuthenticator(InweboMultifactorAuthenticationProperties.BrowserAuthenticatorTypes.M_ACCESS_WEB);
        when(service.loginSearchQuery(LOGIN)).thenReturn(loginSearchOk(5, USER_ID));

        val event = action.execute(requestContext);
        assertEquals(SELECT, event.getId());
        val flowScope = requestContext.getFlowScope();
        assertFalse(flowScope.contains(MUST_ENROLL));
        assertEquals(MA, flowScope.get(BROWSER_AUTHENTICATOR));
    }

    @Test
    void verifyPushAndBrowserNone() throws Throwable {
        inwebo.setBrowserAuthenticator(InweboMultifactorAuthenticationProperties.BrowserAuthenticatorTypes.NONE);
        when(service.loginSearchQuery(LOGIN)).thenReturn(loginSearchOk(5, USER_ID));

        val event = action.execute(requestContext);
        assertEquals(PUSH, event.getId());
        val flowScope = requestContext.getFlowScope();
        assertFalse(flowScope.contains(MUST_ENROLL));
        assertNull(flowScope.get(BROWSER_AUTHENTICATOR));
    }

    @Test
    void verifyPushDisabledAndBrowserVA() throws Throwable {
        inwebo.setPushEnabled(false);
        when(service.loginSearchQuery(LOGIN)).thenReturn(loginSearchOk(5, USER_ID));

        val event = action.execute(requestContext);
        assertEquals(VA, event.getId());
        val flowScope = requestContext.getFlowScope();
        assertFalse(flowScope.contains(MUST_ENROLL));
        assertEquals(VA, flowScope.get(BROWSER_AUTHENTICATOR));
    }

    @Test
    void verifyPushDisabledAndBrowserMA() throws Throwable {
        inwebo.setPushEnabled(false);
        inwebo.setBrowserAuthenticator(InweboMultifactorAuthenticationProperties.BrowserAuthenticatorTypes.M_ACCESS_WEB);
        when(service.loginSearchQuery(LOGIN)).thenReturn(loginSearchOk(5, USER_ID));

        val event = action.execute(requestContext);
        assertEquals(MA, event.getId());
        val flowScope = requestContext.getFlowScope();
        assertFalse(flowScope.contains(MUST_ENROLL));
        assertEquals(MA, flowScope.get(BROWSER_AUTHENTICATOR));
    }

    @Test
    void verifyPushDisabledAndBrowserNone() throws Throwable {
        inwebo.setPushEnabled(false);
        inwebo.setBrowserAuthenticator(InweboMultifactorAuthenticationProperties.BrowserAuthenticatorTypes.NONE);
        when(service.loginSearchQuery(LOGIN)).thenReturn(loginSearchOk(5, USER_ID));

        val event = action.execute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        val flowScope = requestContext.getFlowScope();
        assertFalse(flowScope.contains(MUST_ENROLL));
        assertNull(flowScope.get(BROWSER_AUTHENTICATOR));
    }
}

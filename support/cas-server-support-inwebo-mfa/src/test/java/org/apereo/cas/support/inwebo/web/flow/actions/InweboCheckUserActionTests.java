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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests {@link InweboCheckUserAction}.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Tag("WebflowMfaActions")
public class InweboCheckUserActionTests extends BaseActionTests {

    private static final String SITE_ALIAS = "7845zesf357dsq89s74za6z4e5df";
    private static final int USER_ID = 123456;

    private InweboCheckUserAction action;

    @BeforeEach
    public void setUp() {
        super.setUp();

        val inwebo = new InweboMultifactorAuthenticationProperties();
        inwebo.setSiteAlias(SITE_ALIAS);
        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().setInwebo(inwebo);
        action = new InweboCheckUserAction(service, casProperties);
    }

    private InweboLoginSearchResponse loginSearchOk(final int activationStatus, final int userId) {
        val response = new InweboLoginSearchResponse();
        response.setResult(InweboResult.OK);
        response.setCount(1);
        response.setUserId(userId);
        response.setActivationStatus(activationStatus);
        return response;
    }

    @Test
    public void verifySelectMethod() {
        when(service.loginSearch(LOGIN)).thenReturn(loginSearchOk(3, USER_ID));

        val event = action.doExecute(requestContext);
        assertEquals(WebflowConstants.SELECT, event.getId());
    }

    @Test
    public void verifyPush() {
        when(service.loginSearch(LOGIN)).thenReturn(loginSearchOk(1, USER_ID));

        val event = action.doExecute(requestContext);
        assertEquals(WebflowConstants.PUSH, event.getId());
    }

    @Test
    public void verifyBrowser() {
        when(service.loginSearch(LOGIN)).thenReturn(loginSearchOk(2, USER_ID));

        val event = action.doExecute(requestContext);
        assertEquals(WebflowConstants.BROWSER, event.getId());
    }

    @Test
    public void verifyMustEnroll() {
        when(service.loginSearch(LOGIN)).thenReturn(loginSearchOk(0, USER_ID));

        val event = action.doExecute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        assertTrue((Boolean) requestContext.getFlowScope().get(WebflowConstants.MUST_ENROLL));
    }

    @Test
    public void verifyUserBlocked() {
        val loginSearch = loginSearchOk(3, USER_ID);
        loginSearch.setUserStatus(1);
        when(service.loginSearch(LOGIN)).thenReturn(loginSearch);

        val event = action.doExecute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        assertFalse(requestContext.getFlowScope().contains(WebflowConstants.MUST_ENROLL));
    }

    @Test
    public void verifyUnknownStatus() {
        when(service.loginSearch(LOGIN)).thenReturn(loginSearchOk(4, USER_ID));

        val event = action.doExecute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        assertFalse(requestContext.getFlowScope().contains(WebflowConstants.MUST_ENROLL));
    }

    @Test
    public void verifyNoUser() {
        when(service.loginSearch(LOGIN)).thenReturn(loginSearchOk(3, 0));

        val event = action.doExecute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        assertFalse(requestContext.getFlowScope().contains(WebflowConstants.MUST_ENROLL));
    }

    @Test
    public void verifyInweboException() {
        when(service.loginSearch(LOGIN)).thenThrow(new RuntimeException());

        val event = action.doExecute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        assertFalse(requestContext.getFlowScope().contains(WebflowConstants.MUST_ENROLL));
    }
}

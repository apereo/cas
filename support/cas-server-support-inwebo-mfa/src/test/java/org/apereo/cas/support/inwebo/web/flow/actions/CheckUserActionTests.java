package org.apereo.cas.support.inwebo.web.flow.actions;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.InweboMultifactorProperties;
import org.apereo.cas.support.inwebo.service.response.LoginSearchResponse;
import org.apereo.cas.support.inwebo.service.response.Result;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link CheckUserAction}.
 *
 * @author Jerome LELEU
 * @since 6.3.0
 */
public class CheckUserActionTests extends BaseActionTests {

    private static final String SITE_ALIAS = "7845zesf357dsq89s74za6z4e5df";
    private static final int USER_ID = 123456;

    private CheckUserAction action;

    @BeforeEach
    public void setUp() {
        super.setUp();

        val inwebo = new InweboMultifactorProperties();
        inwebo.setSiteAlias(SITE_ALIAS);
        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().setInwebo(inwebo);
        action = new CheckUserAction(mock(MessageSource.class), service, casProperties);
    }

    private LoginSearchResponse loginSearchOk(final int activationStatus, final int userId) {
        val response = new LoginSearchResponse();
        response.setResult(Result.OK);
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
    public void verifyUserBlocked() {
        when(service.loginSearch(LOGIN)).thenReturn(loginSearchOk(0, USER_ID));

        val event = action.doExecute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        assertTrue((Boolean) requestContext.getFlowScope().get(WebflowConstants.MUST_ENROLL));
        assertTrue(requestContext.getFlowScope().contains(WebflowConstants.INWEBO_ERROR_MESSAGE));
    }

    @Test
    public void verifyUnknownStatus() {
        when(service.loginSearch(LOGIN)).thenReturn(loginSearchOk(4, USER_ID));

        val event = action.doExecute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        assertFalse(requestContext.getFlowScope().contains(WebflowConstants.MUST_ENROLL));
        assertFalse(requestContext.getFlowScope().contains(WebflowConstants.INWEBO_ERROR_MESSAGE));
    }

    @Test
    public void verifyNoUser() {
        when(service.loginSearch(LOGIN)).thenReturn(loginSearchOk(3, 0));

        val event = action.doExecute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        assertFalse(requestContext.getFlowScope().contains(WebflowConstants.MUST_ENROLL));
        assertFalse(requestContext.getFlowScope().contains(WebflowConstants.INWEBO_ERROR_MESSAGE));
    }
}

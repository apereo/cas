package org.apereo.cas.support.inwebo.web.flow.actions;

import org.apereo.cas.support.inwebo.service.response.InweboResult;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.execution.Action;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests {@link InweboCheckAuthenticationAction}.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Tag("WebflowMfaActions")
class InweboCheckAuthenticationActionTests extends BaseInweboActionTests {

    private static final String OTP = "4q5dslf";

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_INWEBO_CHECK_AUTHENTICATION)
    private Action action;
        
    @Test
    void verifyGoodOtp() throws Throwable {
        requestContext.setParameter("otp", OTP);
        when(service.authenticateExtended(LOGIN, OTP)).thenReturn(deviceResponse(InweboResult.OK));

        val event = action.execute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        assertMfa();
    }

    @Test
    void verifyBadOtp() throws Throwable {
        requestContext.setParameter("otp", OTP);
        when(service.authenticateExtended(LOGIN, OTP)).thenReturn(deviceResponse(InweboResult.NOK));

        val event = action.execute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        assertNoMfa();
    }

    @Test
    void verifyPushValidated() throws Throwable {
        requestContext.getFlowScope().put(InweboWebflowConstants.INWEBO_SESSION_ID, SESSION_ID);
        when(service.checkPushResult(LOGIN, SESSION_ID)).thenReturn(deviceResponse(InweboResult.OK));

        val event = action.execute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        assertMfa();
    }

    @Test
    void verifyPushNotValidatedYet() throws Throwable {
        requestContext.getFlowScope().put(InweboWebflowConstants.INWEBO_SESSION_ID, SESSION_ID);
        when(service.checkPushResult(LOGIN, SESSION_ID)).thenReturn(deviceResponse(InweboResult.WAITING));

        val event = action.execute(requestContext);
        assertEquals(InweboWebflowConstants.PENDING, event.getId());
        assertNoMfa();
    }

    @Test
    void verifyPushRefusedOrTimeout() throws Throwable {
        requestContext.getFlowScope().put(InweboWebflowConstants.INWEBO_SESSION_ID, SESSION_ID);
        when(service.checkPushResult(LOGIN, SESSION_ID)).thenReturn(deviceResponse(InweboResult.REFUSED));

        val event = action.execute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        assertNoMfa();
    }

    @Test
    void verifyPushError() throws Throwable {
        requestContext.getFlowScope().put(InweboWebflowConstants.INWEBO_SESSION_ID, SESSION_ID);
        when(service.checkPushResult(LOGIN, SESSION_ID)).thenReturn(deviceResponse(InweboResult.NOK));

        val event = action.execute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        assertNoMfa();
    }
}

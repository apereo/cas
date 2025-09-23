package org.apereo.cas.support.inwebo.web.flow.actions;

import org.apereo.cas.support.inwebo.service.response.InweboPushAuthenticateResponse;
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
 * Tests {@link InweboPushAuthenticateAction}.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Tag("WebflowMfaActions")
class InweboPushAuthenticateActionTests extends BaseInweboActionTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_INWEBO_PUSH_AUTHENTICATION)
    private Action action;
    
    private static InweboPushAuthenticateResponse pushAuthenticateResponse(final InweboResult result) {
        val response = new InweboPushAuthenticateResponse();
        response.setResult(result);
        if (result == InweboResult.OK) {
            response.setSessionId(SESSION_ID);
        }
        return response;
    }

    @Test
    void verifyPushAuthenticateOk() throws Throwable {
        when(service.pushAuthenticate(LOGIN)).thenReturn(pushAuthenticateResponse(InweboResult.OK));

        val event = action.execute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        assertEquals(SESSION_ID, requestContext.getFlowScope().get(InweboWebflowConstants.INWEBO_SESSION_ID));
    }

    @Test
    void verifyPushAuthenticateFailed() throws Throwable {
        when(service.pushAuthenticate(LOGIN)).thenReturn(pushAuthenticateResponse(InweboResult.TIMEOUT));

        val event = action.execute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        assertFalse(requestContext.getFlowScope().contains(InweboWebflowConstants.INWEBO_SESSION_ID));
    }
}

package org.apereo.cas.support.inwebo.web.flow.actions;

import org.apereo.cas.support.inwebo.service.response.InweboPushAuthenticateResponse;
import org.apereo.cas.support.inwebo.service.response.InweboResult;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests {@link InweboPushAuthenticateAction}.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Tag("WebflowMfaActions")
public class InweboPushAuthenticateActionTests extends BaseActionTests {

    private InweboPushAuthenticateAction action;

    @BeforeEach
    public void setUp() {
        super.setUp();

        action = new InweboPushAuthenticateAction(service);
    }

    private InweboPushAuthenticateResponse pushAuthenticateResponse(final InweboResult result) {
        val response = new InweboPushAuthenticateResponse();
        response.setResult(result);
        if (result == InweboResult.OK) {
            response.setSessionId(SESSION_ID);
        }
        return response;
    }

    @Test
    public void verifyPushAuthenticateOk() {
        when(service.pushAuthenticate(LOGIN)).thenReturn(pushAuthenticateResponse(InweboResult.OK));

        val event = action.doExecute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        assertEquals(SESSION_ID, requestContext.getFlowScope().get(WebflowConstants.INWEBO_SESSION_ID));
    }

    @Test
    public void verifyPushAuthenticateFailed() {
        when(service.pushAuthenticate(LOGIN)).thenReturn(pushAuthenticateResponse(InweboResult.TIMEOUT));

        val event = action.doExecute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        assertFalse(requestContext.getFlowScope().contains(WebflowConstants.INWEBO_SESSION_ID));
    }
}

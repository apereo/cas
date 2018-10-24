package org.apereo.cas.web.flow;

import org.apereo.cas.web.flow.logout.TerminateSessionAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link TerminateSessionConfirmingActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = {"cas.tgc.crypto.enabled=false", "cas.logout.confirmLogout=true"})
public class TerminateSessionConfirmingActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier("terminateSessionAction")
    private Action action;

    @Test
    public void verifyTerminateActionConfirmed() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addParameter(TerminateSessionAction.REQUEST_PARAM_LOGOUT_REQUEST_CONFIRMED, "true");
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        WebUtils.putTicketGrantingTicketInScopes(context, "TGT-123456-something");
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
        assertNotNull(WebUtils.getLogoutRequests(context));
    }

    @Test
    public void verifyTerminateActionConfirming() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        WebUtils.putTicketGrantingTicketInScopes(context, "TGT-123456-something");
        assertEquals(CasWebflowConstants.STATE_ID_WARN, action.execute(context).getId());
    }
}

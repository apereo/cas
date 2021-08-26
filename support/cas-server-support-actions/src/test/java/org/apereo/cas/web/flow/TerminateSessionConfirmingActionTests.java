package org.apereo.cas.web.flow;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.flow.logout.TerminateSessionAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TerminateSessionConfirmingActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = {
    "cas.tgc.crypto.enabled=false",
    "cas.logout.confirm-logout=true",
    "cas.logout.redirect-url=https://github.com"
})
@Tag("WebflowActions")
public class TerminateSessionConfirmingActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_TERMINATE_SESSION)
    private Action action;

    @Test
    public void verifyTerminateActionConfirmed() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        Objects.requireNonNull(request.getSession(true)).setAttribute(Pac4jConstants.REQUESTED_URL, "https://github.com");
        request.addParameter(TerminateSessionAction.REQUEST_PARAM_LOGOUT_REQUEST_CONFIRMED, "true");
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        WebUtils.putTicketGrantingTicketInScopes(context, "TGT-123456-something");
        assertEquals(CasWebflowConstants.TRANSITION_ID_REDIRECT, action.execute(context).getId());
    }

    @Test
    public void verifyTerminateActionRequests() throws Exception {
        val tgt = new MockTicketGrantingTicket(RegisteredServiceTestUtils.getAuthentication());
        getTicketRegistry().addTicket(tgt);
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        Objects.requireNonNull(request.getSession(true)).setAttribute(Pac4jConstants.REQUESTED_URL, "https://github.com");
        request.addParameter(TerminateSessionAction.REQUEST_PARAM_LOGOUT_REQUEST_CONFIRMED, "true");
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        WebUtils.putTicketGrantingTicketInScopes(context, tgt.getId());
        WebUtils.putAuthentication(tgt.getAuthentication(), context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_REDIRECT, action.execute(context).getId());
        assertNull(getTicketRegistry().getTicket(tgt.getId()));
        assertTrue(WebUtils.getLogoutRequests(context).isEmpty());
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

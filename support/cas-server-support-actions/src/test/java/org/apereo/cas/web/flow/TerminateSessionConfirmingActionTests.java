package org.apereo.cas.web.flow;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.logout.TerminateSessionAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
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
class TerminateSessionConfirmingActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_TERMINATE_SESSION)
    private Action action;

    @Test
    void verifyTerminateActionConfirmed() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setSessionAttribute(Pac4jConstants.REQUESTED_URL, "https://github.com");
        context.setParameter(TerminateSessionAction.REQUEST_PARAM_LOGOUT_REQUEST_CONFIRMED, "true");
        WebUtils.putTicketGrantingTicketInScopes(context, "TGT-123456-something");
        assertEquals(CasWebflowConstants.TRANSITION_ID_REDIRECT, action.execute(context).getId());
    }

    @Test
    void verifyTerminateActionRequests() throws Throwable {
        val tgt = new MockTicketGrantingTicket(RegisteredServiceTestUtils.getAuthentication());
        getTicketRegistry().addTicket(tgt);
        val context = MockRequestContext.create();
        Objects.requireNonNull(context.getHttpServletRequest().getSession(true)).setAttribute(Pac4jConstants.REQUESTED_URL, "https://github.com");
        context.setParameter(TerminateSessionAction.REQUEST_PARAM_LOGOUT_REQUEST_CONFIRMED, "true");

        WebUtils.putTicketGrantingTicketInScopes(context, tgt.getId());
        WebUtils.putAuthentication(tgt.getAuthentication(), context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_REDIRECT, action.execute(context).getId());
        assertNull(getTicketRegistry().getTicket(tgt.getId()));
        assertTrue(WebUtils.getLogoutRequests(context).isEmpty());
    }

    @Test
    void verifyTerminateActionConfirming() throws Throwable {
        val context = MockRequestContext.create();
        WebUtils.putTicketGrantingTicketInScopes(context, "TGT-123456-something");
        assertEquals(CasWebflowConstants.STATE_ID_WARN, action.execute(context).getId());
    }
}

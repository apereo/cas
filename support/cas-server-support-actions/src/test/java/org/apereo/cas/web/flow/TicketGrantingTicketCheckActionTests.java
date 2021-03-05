package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.web.flow.login.TicketGrantingTicketCheckAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Handles tests for {@link TicketGrantingTicketCheckAction}.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Tag("WebflowActions")
public class TicketGrantingTicketCheckActionTests extends AbstractWebflowActionsTests {

    @Test
    public void verifyNullTicket() throws Exception {
        val ctx = new MockRequestContext();
        val action = new TicketGrantingTicketCheckAction(getCentralAuthenticationService());
        val event = action.execute(ctx);
        assertEquals(CasWebflowConstants.TRANSITION_ID_TICKET_GRANTING_TICKET_NOT_EXISTS, event.getId());
    }

    @Test
    public void verifyInvalidTicket() throws Exception {
        val ctx = new MockRequestContext();
        val tgt = new MockTicketGrantingTicket("user");
        WebUtils.putTicketGrantingTicketInScopes(ctx, tgt);
        val action = new TicketGrantingTicketCheckAction(getCentralAuthenticationService());
        val event = action.execute(ctx);
        assertEquals(CasWebflowConstants.TRANSITION_ID_TICKET_GRANTING_TICKET_INVALID, event.getId());
    }

    @Test
    public void verifyValidTicket() throws Exception {
        val ctx = new MockRequestContext();
        val ctxAuthN = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        val tgt = getCentralAuthenticationService().createTicketGrantingTicket(ctxAuthN);
        WebUtils.putTicketGrantingTicketInScopes(ctx, tgt);
        val action = new TicketGrantingTicketCheckAction(getCentralAuthenticationService());
        val event = action.execute(ctx);
        assertEquals(CasWebflowConstants.TRANSITION_ID_TICKET_GRANTING_TICKET_VALID, event.getId());
    }
}

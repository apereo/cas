package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.login.TicketGrantingTicketCheckAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Handles tests for {@link TicketGrantingTicketCheckAction}.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Tag("WebflowActions")
class TicketGrantingTicketCheckActionTests extends AbstractWebflowActionsTests {

    @BeforeEach
    void setup() throws Exception {
        val services = RegisteredServiceTestUtils.getRegisteredServicesForTests();
        getServicesManager().save(services.stream());
    }

    @Test
    void verifyNullTicket() throws Throwable {
        val ctx = MockRequestContext.create(applicationContext);
        val action = new TicketGrantingTicketCheckAction(getTicketRegistry());
        val event = action.execute(ctx);
        assertEquals(CasWebflowConstants.TRANSITION_ID_TICKET_GRANTING_TICKET_NOT_EXISTS, event.getId());
    }

    @Test
    void verifyInvalidTicket() throws Throwable {
        val ctx = MockRequestContext.create(applicationContext);
        val tgt = new MockTicketGrantingTicket("user");
        WebUtils.putTicketGrantingTicketInScopes(ctx, tgt);
        val action = new TicketGrantingTicketCheckAction(getTicketRegistry());
        val event = action.execute(ctx);
        assertEquals(CasWebflowConstants.TRANSITION_ID_TICKET_GRANTING_TICKET_INVALID, event.getId());
    }

    @Test
    void verifyValidTicket() throws Throwable {
        val ctx = MockRequestContext.create(applicationContext);
        val ctxAuthN = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport());
        val tgt = getCentralAuthenticationService().createTicketGrantingTicket(ctxAuthN);
        WebUtils.putTicketGrantingTicketInScopes(ctx, tgt);
        val action = new TicketGrantingTicketCheckAction(getTicketRegistry());
        val event = action.execute(ctx);
        assertEquals(CasWebflowConstants.TRANSITION_ID_TICKET_GRANTING_TICKET_VALID, event.getId());
    }
}

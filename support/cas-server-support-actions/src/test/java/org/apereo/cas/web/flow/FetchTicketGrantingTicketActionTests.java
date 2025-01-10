package org.apereo.cas.web.flow;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link FetchTicketGrantingTicketActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("WebflowActions")
@TestPropertySource(properties = "cas.tgc.crypto.enabled=false")
class FetchTicketGrantingTicketActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_FETCH_TICKET_GRANTING_TICKET)
    private Action action;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val tgt = new MockTicketGrantingTicket("casuser");
        getTicketRegistry().addTicket(tgt);
        getTicketGrantingTicketCookieGenerator().addCookie(context.getHttpServletRequest(), context.getHttpServletResponse(), tgt.getId());
        context.setRequestCookiesFromResponse();
        assertNull(action.execute(context));
        assertNotNull(WebUtils.getTicketGrantingTicket(context));
    }
}

package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ConfirmLogoutActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("WebflowActions")
class ConfirmLogoutActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier("confirmLogoutAction")
    private Action action;

    @Test
    void verifyDoesNothing() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val result = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        assertNull(WebUtils.getAuthentication(context));
        assertNull(WebUtils.getTicketGrantingTicket(context));
    }

    @Test
    void verifyLocateByContext() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val ticket = new MockTicketGrantingTicket("casuser");
        getTicketRegistry().addTicket(ticket);
        WebUtils.putTicketGrantingTicketInScopes(context, ticket);
        
        val result = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        assertNotNull(WebUtils.getAuthentication(context));
        assertNotNull(WebUtils.getTicketGrantingTicket(context));
    }

    @Test
    void verifyByCookie() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        context.setRemoteAddr("185.86.151.11");
        context.setLocalAddr("185.88.151.11");
        context.withUserAgent();
        context.setClientInfo();

        val ticket = new MockTicketGrantingTicket("casuser");
        getTicketRegistry().addTicket(ticket);

        val cookie = getTicketGrantingTicketCookieGenerator()
            .addCookie(context.getHttpServletRequest(), context.getHttpServletResponse(), ticket.getId());
        context.setHttpRequestCookies(cookie);
        
        val result = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        assertNotNull(WebUtils.getAuthentication(context));
        assertNotNull(WebUtils.getTicketGrantingTicket(context));
    }
}

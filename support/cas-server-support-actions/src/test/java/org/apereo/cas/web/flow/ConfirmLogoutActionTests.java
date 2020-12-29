package org.apereo.cas.web.flow;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ConfirmLogoutActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("WebflowActions")
public class ConfirmLogoutActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier("confirmLogoutAction")
    private Action action;

    @Test
    public void verifyDoesNothing() throws Exception {
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(),
            new MockHttpServletRequest(), new MockHttpServletResponse()));
        val result = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        assertNull(WebUtils.getAuthentication(context));
        assertNull(WebUtils.getTicketGrantingTicket(context));
    }

    @Test
    public void verifyLocateByContext() throws Exception {
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(),
            new MockHttpServletRequest(), new MockHttpServletResponse()));

        val ticket = new MockTicketGrantingTicket("casuser");
        getCentralAuthenticationService().addTicket(ticket);
        WebUtils.putTicketGrantingTicketInScopes(context, ticket);
        
        val result = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        assertNotNull(WebUtils.getAuthentication(context));
        assertNotNull(WebUtils.getTicketGrantingTicket(context));
    }

    @Test
    public void verifyByCookie() throws Exception {
        val context = new MockRequestContext();
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        request.addHeader("User-Agent", "agent");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val ticket = new MockTicketGrantingTicket("casuser");
        getCentralAuthenticationService().addTicket(ticket);

        val c = getTicketGrantingTicketCookieGenerator().addCookie(request, response, ticket.getId());
        request.setCookies(c);
        
        val result = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        assertNotNull(WebUtils.getAuthentication(context));
        assertNotNull(WebUtils.getTicketGrantingTicket(context));
    }
}

package org.apereo.cas.web.flow;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.web.flow.login.SessionStoreTicketGrantingTicketAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SessionStoreTicketGrantingTicketActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowActions")
public class SessionStoreTicketGrantingTicketActionTests extends AbstractWebflowActionsTests {

    @Test
    public void verifyOperation() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        WebUtils.putTicketGrantingTicketInScopes(context, new MockTicketGrantingTicket("casuser"));
        val action = new SessionStoreTicketGrantingTicketAction(JEESessionStore.INSTANCE);
        val result = action.execute(context);
        assertNull(result);
        val webContext = new JEEContext(request, response);
        assertTrue(JEESessionStore.INSTANCE.get(webContext, WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID).isPresent());
    }

}

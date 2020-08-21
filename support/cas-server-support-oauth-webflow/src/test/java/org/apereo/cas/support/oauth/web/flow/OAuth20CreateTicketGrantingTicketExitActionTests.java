package org.apereo.cas.support.oauth.web.flow;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20CreateTicketGrantingTicketExitActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseOAuth20WebflowTests.SharedTestConfiguration.class)
@Tag("OAuth")
public class OAuth20CreateTicketGrantingTicketExitActionTests {
    @Autowired
    @Qualifier("oauth20CreateTicketGrantingTicketExitAction")
    private Action oauth20CreateTicketGrantingTicketExitAction;

    @Autowired
    @Qualifier("oauthDistributedSessionStore")
    private SessionStore<JEEContext> oauthDistributedSessionStore;

    @Test
    public void verifyOperation() throws Exception {
        assertNotNull(oauth20CreateTicketGrantingTicketExitAction);
        assertNotNull(oauthDistributedSessionStore);

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        WebUtils.putTicketGrantingTicketInScopes(context, new MockTicketGrantingTicket("casuser"));
        val result = oauth20CreateTicketGrantingTicketExitAction.execute(context);
        assertNull(result);
        val webContext = new JEEContext(request, response, oauthDistributedSessionStore);
        assertTrue(oauthDistributedSessionStore.get(webContext, WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID).isPresent());
    }

}

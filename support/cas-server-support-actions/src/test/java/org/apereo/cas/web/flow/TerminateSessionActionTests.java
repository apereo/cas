package org.apereo.cas.web.flow;

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

import javax.servlet.http.Cookie;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TerminateSessionActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = "cas.tgc.crypto.enabled=false")
public class TerminateSessionActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier("terminateSessionAction")
    private Action action;

    @Test
    public void verifyTerminateAction() throws Exception {
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));
        WebUtils.putTicketGrantingTicketInScopes(context, "TGT-123456-something");
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
        assertNotNull(WebUtils.getLogoutRequests(context));
    }

    @Test
    public void verifyTerminateActionByCookie() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.setCookies(new Cookie("TGC", "TGT-123456-something"));
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
        assertNotNull(WebUtils.getLogoutRequests(context));
    }
}

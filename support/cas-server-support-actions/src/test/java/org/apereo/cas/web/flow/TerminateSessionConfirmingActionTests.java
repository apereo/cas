package org.apereo.cas.web.flow;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.web.config.CasSupportActionsConfiguration;
import org.apereo.cas.web.flow.logout.TerminateSessionAction;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link TerminateSessionConfirmingActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@DirtiesContext
@Import(CasSupportActionsConfiguration.class)
@Slf4j
@TestPropertySource(properties = {"cas.tgc.crypto.enabled=false", "cas.logout.confirmLogout=true"})
public class TerminateSessionConfirmingActionTests extends AbstractCentralAuthenticationServiceTests {
    @Autowired
    @Qualifier("terminateSessionAction")
    private Action action;

    @Test
    public void verifyTerminateActionConfirmed() throws Exception {
        final var context = new MockRequestContext();
        final var request = new MockHttpServletRequest();
        request.addParameter(TerminateSessionAction.REQUEST_PARAM_LOGOUT_REQUEST_CONFIRMED, "true");
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        WebUtils.putTicketGrantingTicketInScopes(context, "TGT-123456-something");
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
        assertNotNull(WebUtils.getLogoutRequests(context));
    }

    @Test
    public void verifyTerminateActionConfirming() throws Exception {
        final var context = new MockRequestContext();
        final var request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        WebUtils.putTicketGrantingTicketInScopes(context, "TGT-123456-something");
        assertEquals(CasWebflowConstants.STATE_ID_WARN, action.execute(context).getId());
    }
}

package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.config.CasWebflowAccountProfileConfiguration;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AccountProfilePasswordChangeRequestActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("WebflowActions")
@TestPropertySource(properties = "CasFeatureModule.AccountManagement.enabled=true")
@Import(CasWebflowAccountProfileConfiguration.class)
public class AccountProfilePasswordChangeRequestActionTests extends BasePasswordManagementActionTests {
    @Autowired
    @Qualifier("accountProfilePasswordChangeRequestAction")
    private Action accountProfilePasswordChangeRequestAction;

    @Test
    public void verifyOperation() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val tgt = new MockTicketGrantingTicket("casuser");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        WebUtils.putTicketGrantingTicket(context, tgt);
        centralAuthenticationService.addTicket(tgt);

        val result = accountProfilePasswordChangeRequestAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        assertNotNull(WebUtils.getServiceRedirectUrl(context));
        assertThrows(InvalidTicketException.class, () -> centralAuthenticationService.getTicket(tgt.getId()));
    }
}

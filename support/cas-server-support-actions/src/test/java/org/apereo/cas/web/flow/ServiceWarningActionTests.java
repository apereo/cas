package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.flow.login.ServiceWarningAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link ServiceWarningActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class ServiceWarningActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier("serviceWarningAction")
    private Action action;


    private MockRequestContext context;

    @BeforeEach
    public void onSetUp() {
        this.context = new MockRequestContext();
    }

    @Test
    public void verifyAction() throws Exception {
        val request = new MockHttpServletRequest();
        request.addParameter(ServiceWarningAction.PARAMETER_NAME_IGNORE_WARNING, "true");
        this.context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService("https://google.com"));
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());

        val tgt = new MockTicketGrantingTicket("casuser");
        getTicketRegistry().addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(this.context, tgt);

        assertEquals(CasWebflowConstants.STATE_ID_REDIRECT, this.action.execute(this.context).getId());
    }
}

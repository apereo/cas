package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.web.flow.login.ServiceWarningAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ServiceWarningActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowActions")
public class ServiceWarningActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_SERVICE_WARNING)
    private Action action;

    @Test
    public void verifyAction() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addParameter(ServiceWarningAction.PARAMETER_NAME_IGNORE_WARNING, "true");
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        assertThrows(InvalidTicketException.class, () -> action.execute(context).getId());

        val tgt = new MockTicketGrantingTicket("casuser");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        assertThrows(InvalidTicketException.class, () -> action.execute(context).getId());

        val service = RegisteredServiceTestUtils.getService("https://google.com");
        getServicesManager().save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
        
        WebUtils.putServiceIntoFlowScope(context, service);
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());

        getTicketRegistry().addTicket(tgt);

        assertEquals(CasWebflowConstants.STATE_ID_REDIRECT, action.execute(context).getId());
    }
}

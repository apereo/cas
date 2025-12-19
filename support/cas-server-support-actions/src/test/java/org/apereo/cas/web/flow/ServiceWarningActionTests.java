package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.login.ServiceWarningAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ServiceWarningActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowServiceActions")
class ServiceWarningActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_SERVICE_WARNING)
    private Action action;

    @Test
    void verifyAction() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter(ServiceWarningAction.PARAMETER_NAME_IGNORE_WARNING, "true");

        assertThrows(InvalidTicketException.class, () -> action.execute(context));

        val tgt = new MockTicketGrantingTicket("casuser");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        assertThrows(InvalidTicketException.class, () -> action.execute(context));

        val service = RegisteredServiceTestUtils.getService("https://google.com");
        getServicesManager().save(RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of()));
        
        WebUtils.putServiceIntoFlowScope(context, service);
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());

        getTicketRegistry().addTicket(tgt);

        assertEquals(CasWebflowConstants.STATE_ID_REDIRECT, action.execute(context).getId());
    }
}

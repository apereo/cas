package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationConstants;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCredential;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.execution.Action;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasSimpleMultifactorValidateTokenActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
class CasSimpleMultifactorValidateTokenActionTests extends BaseCasSimpleMultifactorSendTokenActionTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_MFA_SIMPLE_VALIDATE_TOKEN)
    private Action action;

    @Test
    void verifyUnknownToken() throws Exception {
        val principal = RegisteredServiceTestUtils.getPrincipal();
        val requestContext = buildRequestContextFor(principal);
        val credential = new CasSimpleMultifactorTokenCredential(UUID.randomUUID().toString());
        WebUtils.putCredential(requestContext, credential);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, action.execute(requestContext).getId());
    }

    @Test
    void verifyUnknownPrincipal() throws Throwable {
        val principal = RegisteredServiceTestUtils.getPrincipal();
        val requestContext = buildRequestContextFor(principal);
        val factory = (CasSimpleMultifactorAuthenticationTicketFactory)
            defaultTicketFactory.get(CasSimpleMultifactorAuthenticationTicket.class);
        val ticket = factory.create(RegisteredServiceTestUtils.getService(),
            Map.of(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL,
                RegisteredServiceTestUtils.getPrincipal("unknown")));
        ticketRegistry.addTicket(ticket);
        val credential = new CasSimpleMultifactorTokenCredential(ticket.getId());
        WebUtils.putCredential(requestContext, credential);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, action.execute(requestContext).getId());
    }

    @Test
    void verifyPassingPrincipal() throws Throwable {
        val principal = RegisteredServiceTestUtils.getPrincipal();
        val requestContext = buildRequestContextFor(principal);
        val factory = (CasSimpleMultifactorAuthenticationTicketFactory)
            defaultTicketFactory.get(CasSimpleMultifactorAuthenticationTicket.class);
        val ticket = factory.create(RegisteredServiceTestUtils.getService(),
            Map.of(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL, principal));
        ticketRegistry.addTicket(ticket);
        val credential = new CasSimpleMultifactorTokenCredential(ticket.getId());
        WebUtils.putCredential(requestContext, credential);
        assertNull(action.execute(requestContext));
    }
}

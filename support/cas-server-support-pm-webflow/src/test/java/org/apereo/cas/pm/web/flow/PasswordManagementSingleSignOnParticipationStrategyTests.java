package org.apereo.cas.pm.web.flow;

import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.web.flow.actions.BasePasswordManagementActionTests;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.SingleSignOnParticipationRequest;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.test.MockRequestContext;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordManagementSingleSignOnParticipationStrategyTests}.
 *
 * @author Julien Huon
 * @since 6.3.0
 */
@Tag("WebflowConfig")
public class PasswordManagementSingleSignOnParticipationStrategyTests extends BasePasswordManagementActionTests {

    @Autowired
    @Qualifier("passwordManagementSingleSignOnParticipationStrategy")
    private SingleSignOnParticipationStrategy strategy;

    @Test
    public void verifyStrategyWithANonPmRequest() {
        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(new MockHttpServletRequest())
            .requestContext(new MockRequestContext())
            .build();
        assertFalse(strategy.supports(ssoRequest));
    }

    @Test
    public void verifyStrategyWithAnInvalidPmRequest() {
        val ctx = new MockRequestContext();
        ctx.putRequestParameter(PasswordManagementWebflowUtils.REQUEST_PARAMETER_NAME_PASSWORD_RESET_TOKEN, "invalidResetToken");

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(new MockHttpServletRequest())
            .requestContext(ctx)
            .build();
        assertTrue(strategy.isParticipating(ssoRequest));
    }

    @Test
    public void verifyStrategyWithAValidPmRequest() {
        val ctx = new MockRequestContext();
        val token = passwordManagementService.createToken(PasswordManagementQuery.builder().username("casuser").build());
        val transientFactory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);
        val serverPrefix = casProperties.getServer().getPrefix();
        val service = webApplicationServiceFactory.createService(serverPrefix);
        val properties = CollectionUtils.<String, Serializable>wrap(PasswordManagementWebflowUtils.FLOWSCOPE_PARAMETER_NAME_TOKEN, token);
        val ticket = transientFactory.create(service, properties);
        ticketRegistry.addTicket(ticket);
        ctx.putRequestParameter(PasswordManagementWebflowUtils.REQUEST_PARAMETER_NAME_PASSWORD_RESET_TOKEN, ticket.getId());

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(new MockHttpServletRequest())
            .requestContext(ctx)
            .build();
        assertFalse(strategy.isParticipating(ssoRequest));
    }
}

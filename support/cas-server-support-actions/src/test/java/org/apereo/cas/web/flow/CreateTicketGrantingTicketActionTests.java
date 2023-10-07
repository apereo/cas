package org.apereo.cas.web.flow;

import org.apereo.cas.DefaultMessageDescriptor;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.execution.Action;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CreateTicketGrantingTicketActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowActions")
class CreateTicketGrantingTicketActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_CREATE_TICKET_GRANTING_TICKET)
    private Action action;

    @Test
    void verifySkipTgt() throws Throwable {
        val context = MockRequestContext.create();

        val tgt = new MockTicketGrantingTicket("casuser-new");
        getTicketRegistry().addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService());
        
        prepareRequestContextForAuthentication(context, tgt.getAuthentication());
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
    }

    @Test
    void verifyCreateTgt() throws Throwable {
        val context = MockRequestContext.create();

        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        prepareRequestContextForAuthentication(context, authentication);
        val tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("TGT-123456");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());

        when(tgt.getId()).thenReturn("TGT-111111");
        val handlerResult = new DefaultAuthenticationHandlerExecutionResult();
        handlerResult.getWarnings().addAll(CollectionUtils.wrapList(new DefaultMessageDescriptor("some.authn.message")));
        authentication.getSuccesses().putAll(CollectionUtils.wrap("handler", handlerResult));
        when(tgt.getAuthentication()).thenReturn(authentication);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS, action.execute(context).getId());
    }

    private static void prepareRequestContextForAuthentication(final MockRequestContext context,
                                                               final Authentication authentication) throws Throwable {
        val builder = mock(AuthenticationResultBuilder.class);
        when(builder.getInitialAuthentication()).thenReturn(Optional.of(authentication));
        when(builder.collect(any(Authentication.class))).thenReturn(builder);

        val result = mock(AuthenticationResult.class);
        when(result.getAuthentication()).thenReturn(authentication);

        when(builder.build(any(PrincipalElectionStrategy.class))).thenReturn(result);
        when(builder.build(any(PrincipalElectionStrategy.class), any(Service.class))).thenReturn(result);

        WebUtils.putAuthenticationResultBuilder(builder, context);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
    }

}

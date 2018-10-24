package org.apereo.cas.web.flow;

import org.apereo.cas.DefaultMessageDescriptor;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
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

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CreateTicketGrantingTicketActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class CreateTicketGrantingTicketActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier("createTicketGrantingTicketAction")
    private Action action;

    private MockRequestContext context;

    @BeforeEach
    public void onSetUp() {
        this.context = new MockRequestContext();
    }

    @Test
    public void verifyCreateTgt() throws Exception {
        this.context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));

        val builder = mock(AuthenticationResultBuilder.class);
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        when(builder.getInitialAuthentication()).thenReturn(Optional.of(authentication));
        when(builder.collect(any(Authentication.class))).thenReturn(builder);

        val result = mock(AuthenticationResult.class);
        when(result.getAuthentication()).thenReturn(authentication);

        when(builder.build(any(PrincipalElectionStrategy.class))).thenReturn(result);
        when(builder.build(any(PrincipalElectionStrategy.class), any(Service.class))).thenReturn(result);

        WebUtils.putAuthenticationResultBuilder(builder, context);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());

        val tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("TGT-123456");
        WebUtils.putTicketGrantingTicketInScopes(this.context, tgt);

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, this.action.execute(this.context).getId());

        when(tgt.getId()).thenReturn("TGT-111111");
        val handlerResult = new DefaultAuthenticationHandlerExecutionResult();
        handlerResult.getWarnings().addAll(CollectionUtils.wrapList(new DefaultMessageDescriptor("some.authn.message")));
        authentication.getSuccesses().putAll(CollectionUtils.wrap("handler", handlerResult));
        when(tgt.getAuthentication()).thenReturn(authentication);
        WebUtils.putTicketGrantingTicketInScopes(this.context, tgt);

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS, this.action.execute(this.context).getId());
    }

}

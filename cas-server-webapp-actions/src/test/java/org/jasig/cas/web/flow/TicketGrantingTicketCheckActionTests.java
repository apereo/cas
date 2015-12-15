package org.jasig.cas.web.flow;

import org.jasig.cas.AbstractCentralAuthenticationServiceTests;
import org.jasig.cas.authentication.AuthenticationContext;
import org.jasig.cas.authentication.AuthenticationContextBuilder;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.AuthenticationTransaction;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.DefaultAuthenticationContextBuilder;
import org.jasig.cas.authentication.TestUtils;
import org.jasig.cas.mock.MockTicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.web.support.WebUtils;
import org.junit.Test;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * Handles tests for {@link TicketGrantingTicketCheckAction}.
 *
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.1.0
 */
public class TicketGrantingTicketCheckActionTests extends AbstractCentralAuthenticationServiceTests {

    @Test
    public void verifyNullTicket() throws Exception {

        final MockRequestContext ctx = new MockRequestContext();
        final TicketGrantingTicketCheckAction action = new
                TicketGrantingTicketCheckAction(this.getCentralAuthenticationService());
        final Event event = action.doExecute(ctx);
        assertEquals(event.getId(), TicketGrantingTicketCheckAction.NOT_EXISTS);
    }

    @Test
    public void verifyInvalidTicket() throws Exception {

        final MockRequestContext ctx = new MockRequestContext();
        final MockTicketGrantingTicket tgt = new MockTicketGrantingTicket("user");

        WebUtils.putTicketGrantingTicketInScopes(ctx, tgt);
        final TicketGrantingTicketCheckAction action = new
                TicketGrantingTicketCheckAction(this.getCentralAuthenticationService());
        final Event event = action.doExecute(ctx);
        assertEquals(event.getId(), TicketGrantingTicketCheckAction.INVALID);
    }

    @Test
    public void verifyValidTicket() throws Exception {

        final MockRequestContext ctx = new MockRequestContext();
        final AuthenticationContext ctxAuthN = getAuthenticationContext(org.jasig.cas.authentication
                .TestUtils.getCredentialsWithSameUsernameAndPassword());

        final TicketGrantingTicket tgt = this.getCentralAuthenticationService()
                .createTicketGrantingTicket(ctxAuthN);

        WebUtils.putTicketGrantingTicketInScopes(ctx, tgt);
        final TicketGrantingTicketCheckAction action = new
                TicketGrantingTicketCheckAction(this.getCentralAuthenticationService());
        final Event event = action.doExecute(ctx);
        assertEquals(event.getId(), TicketGrantingTicketCheckAction.VALID);
    }

    private AuthenticationContext getAuthenticationContext(final Credential... credentials)
            throws AuthenticationException {
        final AuthenticationContextBuilder builder = new DefaultAuthenticationContextBuilder(
                getAuthenticationObjectsRepository().getPrincipalElectionStrategy());
        final AuthenticationTransaction transaction =
                getAuthenticationObjectsRepository().getAuthenticationTransactionFactory()
                        .get(org.jasig.cas.authentication.TestUtils.getCredentialsWithSameUsernameAndPassword());
        getAuthenticationObjectsRepository().getAuthenticationTransactionManager()
                .handle(transaction,  builder);
        final AuthenticationContext ctx = builder.build(TestUtils.getService());
        return ctx;
    }

}

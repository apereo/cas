package org.jasig.cas.web.flow;

import org.jasig.cas.AbstractCentralAuthenticationServiceTests;
import org.jasig.cas.authentication.AuthenticationContext;
import org.jasig.cas.authentication.AuthenticationContextBuilder;
import org.jasig.cas.authentication.AuthenticationTransaction;
import org.jasig.cas.authentication.DefaultAuthenticationContextBuilder;
import org.jasig.cas.authentication.TestUtils;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.web.support.WebUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import javax.servlet.http.Cookie;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public final class GenerateServiceTicketActionTests extends AbstractCentralAuthenticationServiceTests {

    private GenerateServiceTicketAction action;

    private TicketGrantingTicket ticketGrantingTicket;

    @Before
    public void onSetUp() throws Exception {
        this.action = new GenerateServiceTicketAction();
        this.action.setCentralAuthenticationService(getCentralAuthenticationService());
        this.action.setAuthenticationSystemSupport(getAuthenticationSystemSupport());
        this.action.setTicketRegistrySupport(getTicketRegistrySupport());
        this.action.afterPropertiesSet();

        final AuthenticationContextBuilder builder = new DefaultAuthenticationContextBuilder(
                getAuthenticationSystemSupport().getPrincipalElectionStrategy());
        final AuthenticationTransaction transaction = AuthenticationTransaction.wrap(TestUtils.getCredentialsWithSameUsernameAndPassword());
        getAuthenticationSystemSupport().getAuthenticationTransactionManager()
                .handle(transaction,  builder);
        final AuthenticationContext ctx = builder.build(TestUtils.getService());
        this.ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        getTicketRegistry().addTicket(this.ticketGrantingTicket);
    }

    @Test
    public void verifyServiceTicketFromCookie() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.getFlowScope().put("service", org.jasig.cas.services.TestUtils.getService());
        context.getFlowScope().put("ticketGrantingTicketId", this.ticketGrantingTicket.getId());
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter("service", "service");
        request.setCookies(new Cookie("TGT", this.ticketGrantingTicket.getId()));

        this.action.execute(context);

        assertNotNull(WebUtils.getServiceTicketFromRequestScope(context));
    }

    @Test
    public void verifyTicketGrantingTicketFromRequest() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.getFlowScope().put("service", org.jasig.cas.services.TestUtils.getService());
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter("service", "service");
        WebUtils.putTicketGrantingTicketInScopes(context,
                this.ticketGrantingTicket);

        this.action.execute(context);

        assertNotNull(WebUtils.getServiceTicketFromRequestScope(context));
    }

    @Test
    public void verifyTicketGrantingTicketNoTgt() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.getFlowScope().put("service", org.jasig.cas.services.TestUtils.getService());
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter("service", "service");

        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("bleh");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);

        assertEquals("error", this.action.execute(context).getId());
    }

    @Test
    public void verifyTicketGrantingTicketExpiredTgt() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.getFlowScope().put("service", org.jasig.cas.services.TestUtils.getService());
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter("service", "service");
        WebUtils.putTicketGrantingTicketInScopes(context, this.ticketGrantingTicket);

        this.ticketGrantingTicket.markTicketExpired();
        assertEquals("error", this.action.execute(context).getId());
    }
    
    @Test
    public void verifyTicketGrantingTicketNotTgtButGateway() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.getFlowScope().put("service", org.jasig.cas.services.TestUtils.getService());
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter("service", "service");
        request.addParameter("gateway", "true");
        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("bleh");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);


        assertEquals("gateway", this.action.execute(context).getId());
    }
}

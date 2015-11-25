package org.jasig.cas.support.openid.web.flow;


import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.support.openid.AbstractOpenIdTests;
import org.jasig.cas.support.openid.OpenIdProtocolConstants;
import org.jasig.cas.support.openid.authentication.principal.OpenIdService;
import org.jasig.cas.support.openid.authentication.principal.OpenIdServiceFactory;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class OpenIdSingleSignOnActionTests extends AbstractOpenIdTests {

    @Autowired
    private OpenIdSingleSignOnAction action;

    @Autowired
    private TicketRegistry ticketRegistry;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CentralAuthenticationService impl;

    @Test
    public void verifyNoTgt() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), new MockHttpServletRequest(),
                new MockHttpServletResponse()));
        assertEquals("error", this.action.execute(context).getId());
    }

    @Test
    public void verifyNoService() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request,
                new MockHttpServletResponse()));
        final Event event = this.action.execute(context);

        assertNotNull(event);

        assertEquals("error", this.action.execute(context).getId());
    }

    @Test
    public void verifyBadUsername() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(OpenIdProtocolConstants.OPENID_IDENTITY, "fablah");
        request.setParameter(OpenIdProtocolConstants.OPENID_RETURNTO, "http://www.cnn.com");

        final OpenIdServiceFactory factory = new OpenIdServiceFactory();
        final OpenIdService service = factory.createService(request);
        context.getFlowScope().put("service", service);
        context.getFlowScope().put("ticketGrantingTicketId", "tgtId");

        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request,
                new MockHttpServletResponse()));
        assertEquals("error", this.action.execute(context).getId());
    }

    @Test
    public void verifySuccessfulServiceTicket() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final Authentication authentication = org.jasig.cas.authentication.TestUtils.getAuthentication("scootman28");
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("TGT-11", authentication,
                new NeverExpiresExpirationPolicy());

        this.ticketRegistry.addTicket(t);

        request.setParameter(OpenIdProtocolConstants.OPENID_IDENTITY, "http://openid.aol.com/scootman28");
        request.setParameter(OpenIdProtocolConstants.OPENID_RETURNTO, "http://www.cnn.com");

        final OpenIdService service = new OpenIdServiceFactory().createService(request);
        context.getFlowScope().put("service", service);
        context.getFlowScope().put("ticketGrantingTicketId", t.getId());

        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request,
                new MockHttpServletResponse()));
        assertEquals("success", this.action.execute(context).getId());
    }
}

package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.support.openid.AbstractOpenIdTests;
import org.apereo.cas.support.openid.OpenIdProtocolConstants;
import org.apereo.cas.support.openid.authentication.principal.OpenIdServiceFactory;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.flow.config.OpenIdWebflowConfiguration;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 * @deprecated 6.2
 */
@Tag("WebflowActions")
@Deprecated(since = "6.2.0")
@SpringBootTest(classes = {
    AbstractOpenIdTests.SharedTestConfiguration.class,
    OpenIdWebflowConfiguration.class
})
public class OpenIdSingleSignOnActionTests {

    @Autowired
    @Qualifier("openIdSingleSignOnAction")
    private Action action;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Test
    public void verifyNoTgt() throws Exception {
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), new MockHttpServletRequest(),
            new MockHttpServletResponse()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, this.action.execute(context).getId());
    }

    @Test
    public void verifyNoService() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), request,
            new MockHttpServletResponse()));
        val event = this.action.execute(context);

        assertNotNull(event);

        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, this.action.execute(context).getId());
    }

    @Test
    public void verifyBadUsername() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.setParameter(OpenIdProtocolConstants.OPENID_IDENTITY, "fablah");
        request.setParameter(OpenIdProtocolConstants.OPENID_RETURNTO, "http://www.cnn.com");

        val factory = new OpenIdServiceFactory(StringUtils.EMPTY);
        val service = factory.createService(request);
        WebUtils.putServiceIntoFlowScope(context, service);
        context.getFlowScope().put(WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, "tgtId");

        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), request,
            new MockHttpServletResponse()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, this.action.execute(context).getId());
    }

    @Test
    public void verifySuccessfulServiceTicket() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val authentication = CoreAuthenticationTestUtils.getAuthentication("scootman28");
        val t = new TicketGrantingTicketImpl("TGT-11", authentication, NeverExpiresExpirationPolicy.INSTANCE);

        this.ticketRegistry.addTicket(t);

        request.setParameter(OpenIdProtocolConstants.OPENID_IDENTITY, "http://openid.aol.com/scootman28");
        request.setParameter(OpenIdProtocolConstants.OPENID_RETURNTO, "https://google.com");

        val service = new OpenIdServiceFactory().createService(request);
        WebUtils.putServiceIntoFlowScope(context, service);
        context.getFlowScope().put(WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, t.getId());
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_GENERATE_SERVICE_TICKET, this.action.execute(context).getId());
    }
}

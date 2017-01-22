package org.apereo.cas.web;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.ProxyGrantingTicketImpl;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class ProxyControllerTests extends AbstractCentralAuthenticationServiceTests {

    private ProxyController proxyController;

    @Before
    public void onSetUp() throws Exception {
        this.proxyController = new ProxyController(getCentralAuthenticationService(), getWebApplicationServiceFactory());
        final StaticApplicationContext context = new StaticApplicationContext();
        context.refresh();
        this.proxyController.setApplicationContext(context);
    }

    @Test
    public void verifyNoParams() throws Exception {
        assertEquals(CasProtocolConstants.ERROR_CODE_INVALID_REQUEST_PROXY, this.proxyController
                .handleRequestInternal(new MockHttpServletRequest(),
                        new MockHttpServletResponse()).getModel()
                        .get("code"));
    }

    @Test
    public void verifyNonExistentPGT() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET, "TestService");
        request.addParameter("targetService", "testDefault");

        assertTrue(this.proxyController.handleRequestInternal(request,
                new MockHttpServletResponse()).getModel().containsKey(
                        "code"));
    }

    @Test
    public void verifyExistingPGT() throws Exception {
        final ProxyGrantingTicket ticket = new ProxyGrantingTicketImpl(
                "ticketGrantingTicketId", CoreAuthenticationTestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        getTicketRegistry().addTicket(ticket);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET, ticket.getId());
        request.addParameter("targetService", "testDefault");

        assertTrue(this.proxyController.handleRequestInternal(request,
                new MockHttpServletResponse()).getModel().containsKey(
                CasProtocolConstants.PARAMETER_TICKET));
    }

    @Test
    public void verifyNotAuthorizedPGT() throws Exception {
        final ProxyGrantingTicket ticket = new ProxyGrantingTicketImpl("ticketGrantingTicketId",
                CoreAuthenticationTestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        getTicketRegistry().addTicket(ticket);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET, ticket.getId());
        request.addParameter("targetService", "service");

        final Map<String, Object> map = this.proxyController.handleRequestInternal(request,
                new MockHttpServletResponse()).getModel();
        assertFalse(map.containsKey(CasProtocolConstants.PARAMETER_TICKET));
    }
}

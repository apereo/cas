package org.jasig.cas.web;

import java.util.Map;

import org.jasig.cas.AbstractCentralAuthenticationServiceTests;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.jasig.cas.ticket.ProxyGrantingTicketImpl;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class ProxyControllerTests extends AbstractCentralAuthenticationServiceTests {

    private ProxyController proxyController;

    @Before
    public void onSetUp() throws Exception {
        this.proxyController = new ProxyController();
        this.proxyController
        .setCentralAuthenticationService(getCentralAuthenticationService());

        final StaticApplicationContext context = new StaticApplicationContext();
        context.refresh();
        this.proxyController.setApplicationContext(context);
    }

    @Test
    public void verifyNoParams() throws Exception {
        assertEquals("INVALID_REQUEST", this.proxyController
                .handleRequestInternal(new MockHttpServletRequest(),
                        new MockHttpServletResponse()).getModel()
                        .get("code"));
    }

    @Test
    public void verifyNonExistentPGT() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("pgt", "TestService");
        request.addParameter("targetService", "testDefault");

        assertTrue(this.proxyController.handleRequestInternal(request,
                new MockHttpServletResponse()).getModel().containsKey(
                        "code"));
    }

    @Test
    public void verifyExistingPGT() throws Exception {
        final ProxyGrantingTicket ticket = new ProxyGrantingTicketImpl(
                "ticketGrantingTicketId", org.jasig.cas.authentication.TestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        getTicketRegistry().addTicket(ticket);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("pgt", ticket.getId());
        request.addParameter("targetService", "testDefault");

        assertTrue(this.proxyController.handleRequestInternal(request,
                new MockHttpServletResponse()).getModel().containsKey(
                        "ticket"));
    }

    @Test
    public void verifyNotAuthorizedPGT() throws Exception {
        final ProxyGrantingTicket ticket = new ProxyGrantingTicketImpl("ticketGrantingTicketId",
                org.jasig.cas.authentication.TestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        getTicketRegistry().addTicket(ticket);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("pgt", ticket.getId());
        request.addParameter("targetService", "service");

        final Map<String, Object> map = this.proxyController.handleRequestInternal(request,  new MockHttpServletResponse()).getModel();
        assertTrue(!map.containsKey("ticket"));
    }
}

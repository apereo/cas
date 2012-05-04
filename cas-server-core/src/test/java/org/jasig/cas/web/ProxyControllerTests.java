/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class ProxyControllerTests extends AbstractCentralAuthenticationServiceTest {

    private ProxyController proxyController;

    @Before
    public void onSetUp() throws Exception {
        this.proxyController = new ProxyController();
        this.proxyController
            .setCentralAuthenticationService(getCentralAuthenticationService());

        StaticApplicationContext context = new StaticApplicationContext();
        context.refresh();
        this.proxyController.setApplicationContext(context);
    }

    @Test
    public void testNoParams() throws Exception {
        assertEquals("INVALID_REQUEST", this.proxyController
            .handleRequestInternal(new MockHttpServletRequest(),
                new MockHttpServletResponse()).getModel()
            .get("code"));
    }

    @Test
    public void testNonExistentPGT() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("pgt", "TestService");
        request.addParameter("targetService", "service");

        assertTrue(this.proxyController.handleRequestInternal(request,
            new MockHttpServletResponse()).getModel().containsKey(
            "code"));
    }

    @Test
    public void testExistingPGT() throws Exception {
        final TicketGrantingTicket ticket = new TicketGrantingTicketImpl(
            "ticketGrantingTicketId", TestUtils.getAuthentication(),
            new NeverExpiresExpirationPolicy());
        getTicketRegistry().addTicket(ticket);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request
            .addParameter("pgt", ticket.getId());
        request.addParameter(
            "targetService", "service");

        assertTrue(this.proxyController.handleRequestInternal(request,
            new MockHttpServletResponse()).getModel().containsKey(
            "ticket"));
    }
}

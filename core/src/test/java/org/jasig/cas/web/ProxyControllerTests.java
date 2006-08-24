/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.CookieGenerator;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class ProxyControllerTests extends
    AbstractCentralAuthenticationServiceTest {

    private ProxyController proxyController;

    private CasArgumentExtractor casArgumentExtractor;

    protected void onSetUp() throws Exception {
        this.proxyController = new ProxyController();
        this.proxyController
            .setCentralAuthenticationService(getCentralAuthenticationService());
        this.casArgumentExtractor = new CasArgumentExtractor(new CookieGenerator(), new CookieGenerator());
        this.proxyController.setCasArgumentExtractor(this.casArgumentExtractor);
        this.proxyController.afterPropertiesSet();

        StaticApplicationContext context = new StaticApplicationContext();
        context.refresh();
        ((ApplicationContextAware) this.proxyController)
            .setApplicationContext(context);
    }

    public void testNoParams() throws Exception {
        assertEquals("INVALID_REQUEST", this.proxyController
            .handleRequestInternal(new MockHttpServletRequest(),
                new MockHttpServletResponse()).getModel()
            .get("code"));
    }

    public void testNonExistantPGT() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(this.casArgumentExtractor.getProxyGrantingTicketParameterName(), "TestService");
        request.addParameter(this.casArgumentExtractor
            .getTargetServiceParameterName(), "service");

        assertTrue(this.proxyController.handleRequestInternal(request,
            new MockHttpServletResponse()).getModel().containsKey(
            "code"));
    }

    public void testExistingPGT() throws Exception {
        final TicketGrantingTicket ticket = new TicketGrantingTicketImpl(
            "ticketGrantingTicketId", TestUtils.getAuthentication(),
            new NeverExpiresExpirationPolicy());
        getTicketRegistry().addTicket(ticket);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request
            .addParameter(this.casArgumentExtractor.getProxyGrantingTicketParameterName(), ticket.getId());
        request.addParameter(this.casArgumentExtractor
            .getTargetServiceParameterName(), "service");

        assertTrue(this.proxyController.handleRequestInternal(request,
            new MockHttpServletResponse()).getModel().containsKey(
            "ticket"));
    }
}

/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import org.jasig.cas.CentralAuthenticationServiceImpl;
import org.jasig.cas.mock.MockAuthentication;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class ProxyControllerTests extends TestCase {

    private ProxyController proxyController;
    
    private TicketRegistry t;

    protected void setUp() throws Exception {
        this.t = new DefaultTicketRegistry();
        CentralAuthenticationServiceImpl c = new CentralAuthenticationServiceImpl();
        c.setTicketRegistry(this.t);
        c.setUniqueTicketIdGenerator(new DefaultUniqueTicketIdGenerator());
        c.setServiceTicketExpirationPolicy(new NeverExpiresExpirationPolicy());

        this.proxyController = new ProxyController();
        this.proxyController.setCentralAuthenticationService(c);
        this.proxyController.afterPropertiesSet();

        StaticApplicationContext context = new StaticApplicationContext();
        context.refresh();
        ((ApplicationContextAware) this.proxyController).setApplicationContext(context);
    }

    public void testAfterPropertiesSet() {
        ProxyController controller = new ProxyController();
        
        try {
            controller.afterPropertiesSet();
            fail("IllegalArgumentException expected.");
        } catch (Exception e) {
            return;
        }
    }

    public void testNonExistantPGT() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(WebConstants.PROXY_GRANTING_TICKET, "TestService");
        request.addParameter(WebConstants.TARGET_SERVICE, "service");
        
        assertTrue(this.proxyController.handleRequestInternal(request, new MockHttpServletResponse()).getModel().containsKey(WebConstants.CODE));
    }
    
    public void testExistingPGT() throws Exception {
        final TicketGrantingTicket ticket = new TicketGrantingTicketImpl("ticketGrantingTicketId", new MockAuthentication(), new NeverExpiresExpirationPolicy());
        this.t.addTicket(ticket);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(WebConstants.PROXY_GRANTING_TICKET, ticket.getId());
        request.addParameter(WebConstants.TARGET_SERVICE, "service");
        
        assertTrue(this.proxyController.handleRequestInternal(request, new MockHttpServletResponse()).getModel().containsKey(WebConstants.TICKET));
    }
}

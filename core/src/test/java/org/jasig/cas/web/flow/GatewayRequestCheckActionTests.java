/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import org.jasig.cas.web.util.SecureCookieGenerator;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.execution.servlet.ServletEvent;
import org.springframework.webflow.test.MockRequestContext;

import junit.framework.TestCase;


public class GatewayRequestCheckActionTests extends TestCase {
    private GatewayRequestCheckAction action = new GatewayRequestCheckAction();
    
    protected void setUp() throws Exception {
        this.action.setTicketGrantingTicketCookieGenerator(new SecureCookieGenerator());
        this.action.setWarnCookieGenerator(new SecureCookieGenerator());
        this.action.afterPropertiesSet();
    }
    
    public void testGatewayIsTrue() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();
        context.setSourceEvent(new ServletEvent(request, new MockHttpServletResponse()));
        request.addParameter("gateway", "true");
        request.addParameter("service", "test");
        
        assertEquals("gateway", this.action.execute(context).getId());
    }

    public void testGatewayIsFalse() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();
        context.setSourceEvent(new ServletEvent(request, new MockHttpServletResponse()));
        request.addParameter("service", "test");
        
        assertEquals("authenticationRequired", this.action.execute(context).getId());
    }
    
    public void testGatewayIsTrueNoService() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();
        context.setSourceEvent(new ServletEvent(request, new MockHttpServletResponse()));
        
        assertEquals("authenticationRequired", this.action.execute(context).getId());
    }    
    
}

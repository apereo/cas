/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.client;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import junit.framework.TestCase;

public final class ServiceTicketNonInteractiveCredentialsActionTests extends TestCase  {
    
    private ServiceTicketNonInteractiveCredentialsAction action = new ServiceTicketNonInteractiveCredentialsAction();
    
    public void testTicketPresent() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        
        request.addParameter("ticket", "ticket");
        assertNotNull(this.action.constructCredentialsFromRequest(context));
    }
    
    public void testTicketNotPresent() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        
        request.addParameter("ticket1", "ticket");
        assertNull(this.action.constructCredentialsFromRequest(context));
    }
}
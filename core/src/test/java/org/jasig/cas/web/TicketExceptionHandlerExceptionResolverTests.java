/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import org.jasig.cas.ticket.TicketNotFoundException;
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
public class TicketExceptionHandlerExceptionResolverTests extends TestCase {
    private TicketExceptionHandlerExceptionResolver resolver = new TicketExceptionHandlerExceptionResolver();
    
    public void testResolverException() {
        assertNull(this.resolver.resolveException(new MockHttpServletRequest(), new MockHttpServletResponse(), null, new Exception()));
    }

    public void testResolverTicketException() {
        assertNotNull(this.resolver.resolveException(new MockHttpServletRequest(), new MockHttpServletResponse(), null, new TicketNotFoundException()));
    }

    
}

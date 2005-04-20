/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;

import junit.framework.TestCase;

public class TicketNotFoundExceptionTests extends TestCase {

    public void testCodeNoThrowable() {
        TicketException t = new TicketNotFoundException();
        assertEquals("INVALID_TICKET", t.getCode());
    }

    public void testCodeWithThrowable() {
        AuthenticationException a = new BadCredentialsAuthenticationException();
        TicketException t = new TicketNotFoundException(a);

        assertEquals(a.toString(), t.getCode());
    }
}

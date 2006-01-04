/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class BadCredentialsAuthenticationExceptionTests extends TestCase {

    public void testGetCode() {
        AuthenticationException e = new BadCredentialsAuthenticationException();
        assertEquals("error.authentication.credentials.bad", e.getCode());
        assertEquals("error.authentication.credentials.bad", e.toString());
    }
}

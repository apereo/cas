/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication.handler;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision: 39552 $ $Date: 2007-01-22 15:35:37 -0500 (Mon, 22 Jan 2007) $
 * @since 3.1
 */
public final class BlockedCredentialsAuthenticationExceptionTests extends TestCase {
    
    private static final String CODE = "error.authentication.credentials.blocked";

    public void testGetCode() {
        AuthenticationException e = new BlockedCredentialsAuthenticationException ();
        assertEquals(CODE, e.getCode());
        assertEquals(CODE, e.toString());
    }
    
    public void testThrowableConstructor() {
        final RuntimeException r = new RuntimeException();
        final BlockedCredentialsAuthenticationException e = new BlockedCredentialsAuthenticationException(r);
        
        assertEquals(CODE, e.getCode());
        assertEquals(r, e.getCause());
    }
    
    public void testCodeConstructor() {
        final String MESSAGE = "GG";
        final BlockedCredentialsAuthenticationException e = new BlockedCredentialsAuthenticationException(MESSAGE);
        
        assertEquals(MESSAGE, e.getCode());
    }
    
    public void testThrowableConstructorWithCode() {
        final String MESSAGE = "GG";
        final RuntimeException r = new RuntimeException();
        final BlockedCredentialsAuthenticationException e = new BlockedCredentialsAuthenticationException(MESSAGE, r);
        
        assertEquals(MESSAGE, e.getCode());
        assertEquals(r, e.getCause());
    }
}

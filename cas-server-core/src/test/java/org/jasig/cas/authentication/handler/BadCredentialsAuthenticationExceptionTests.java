/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication.handler;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class BadCredentialsAuthenticationExceptionTests extends TestCase {

    private static final String CODE = "error.authentication.credentials.bad";
    
    public void testGetCode() {
        AuthenticationException e = new BadCredentialsAuthenticationException();
        assertEquals(CODE, e.getCode());
        assertEquals(CODE, e.toString());
    }
    
    public void testThrowableConstructor() {
        final RuntimeException r = new RuntimeException();
        final BadCredentialsAuthenticationException e = new BadCredentialsAuthenticationException(r);
        
        assertEquals(CODE, e.getCode());
        assertEquals(r, e.getCause());
    }
    
    public void testCodeConstructor() {
        final String MESSAGE = "GG";
        final BadCredentialsAuthenticationException e = new BadCredentialsAuthenticationException(MESSAGE);
        
        assertEquals(MESSAGE, e.getCode());
    }
    
    public void testThrowableConstructorWithCode() {
        final String MESSAGE = "GG";
        final RuntimeException r = new RuntimeException();
        final BadCredentialsAuthenticationException e = new BadCredentialsAuthenticationException(MESSAGE, r);
        
        assertEquals(MESSAGE, e.getCode());
        assertEquals(r, e.getCause());
    }
}

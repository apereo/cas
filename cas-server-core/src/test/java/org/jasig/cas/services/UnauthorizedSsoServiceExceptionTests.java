/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import junit.framework.TestCase;


public class UnauthorizedSsoServiceExceptionTests extends TestCase {

    private static final String CODE = "service.not.authorized.sso";
    
    public void testGetCode() {
        UnauthorizedSsoServiceException e = new UnauthorizedSsoServiceException();
        assertEquals(CODE, e.getMessage());
    }

    public void testCodeConstructor() {
        final String MESSAGE = "GG";
        final UnauthorizedSsoServiceException e = new UnauthorizedSsoServiceException(MESSAGE);
        
        assertEquals(MESSAGE, e.getMessage());
    }
    
    public void testThrowableConstructorWithCode() {
        final String MESSAGE = "GG";
        final RuntimeException r = new RuntimeException();
        final UnauthorizedSsoServiceException e = new UnauthorizedSsoServiceException(MESSAGE, r);
        
        assertEquals(MESSAGE, e.getMessage());
        assertEquals(r, e.getCause());
    }
}

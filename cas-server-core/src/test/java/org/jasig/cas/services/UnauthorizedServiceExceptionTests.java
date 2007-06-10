/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import junit.framework.TestCase;


public class UnauthorizedServiceExceptionTests extends TestCase {

    private static final String CODE = "service.not.authorized";
    
    public void testGetCode() {
        UnauthorizedServiceException e = new UnauthorizedServiceException();
        assertEquals(CODE, e.getMessage());
    }

    public void testCodeConstructor() {
        final String MESSAGE = "GG";
        final UnauthorizedServiceException e = new UnauthorizedServiceException(MESSAGE);
        
        assertEquals(MESSAGE, e.getMessage());
    }
    
    public void testThrowableConstructorWithCode() {
        final String MESSAGE = "GG";
        final RuntimeException r = new RuntimeException();
        final UnauthorizedServiceException e = new UnauthorizedServiceException(MESSAGE, r);
        
        assertEquals(MESSAGE, e.getMessage());
        assertEquals(r, e.getCause());
    }
}

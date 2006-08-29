/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.services;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class UnauthorizedServiceExceptionTests extends TestCase {

    public void testNoParamConstructor() {
        new UnauthorizedServiceException();
    }

    public void testMessageParamConstructor() {
        final String MESSAGE = "Test";
        UnauthorizedServiceException e = new UnauthorizedServiceException(
            MESSAGE);
        assertEquals(MESSAGE, e.getMessage());
    }

    public void testMessageThrowableConstructor() {
        final String MESSAGE = "test";
        final Throwable THROWABLE = new Throwable();
        UnauthorizedServiceException e = new UnauthorizedServiceException(
            MESSAGE, THROWABLE);

        assertEquals(MESSAGE, e.getMessage());
        assertEquals(THROWABLE, e.getCause());
    }

    public void testThrowableConstructor() {
        final Throwable THROWABLE = new Throwable();
        UnauthorizedServiceException e = new UnauthorizedServiceException(
            THROWABLE);

        assertEquals(THROWABLE, e.getCause());
    }
}

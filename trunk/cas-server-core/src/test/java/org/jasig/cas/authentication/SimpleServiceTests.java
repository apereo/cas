/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication;

import org.jasig.cas.TestUtils;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class SimpleServiceTests extends TestCase {

    public void testProperId() {
        assertEquals("Ids are not equal.", TestUtils.CONST_USERNAME, TestUtils
            .getService().getId());
    }

    public void testEqualsWithNull() {
        assertFalse("Service matches null.", TestUtils.getService()
            .equals(null));
    }

    public void testEqualsWithBadClass() {
        assertFalse("Services matches String class.", TestUtils.getService()
            .equals(new Object()));
    }

    public void testEquals() {
        assertTrue("Services are not equal.", TestUtils.getService().equals(
            TestUtils.getService()));
    }
}
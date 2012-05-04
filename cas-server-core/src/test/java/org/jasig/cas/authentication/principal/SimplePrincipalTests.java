/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication.principal;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class SimplePrincipalTests extends TestCase {

    public void testProperId() {
        final String id = "test";
        assertEquals(id, new SimplePrincipal(id).getId());
    }

    public void testEqualsWithNull() {
        assertFalse(new SimplePrincipal("test").equals(null));
    }

    public void testEqualsWithBadClass() {
        assertFalse(new SimplePrincipal("test").equals("test"));
    }

    public void testEquals() {
        assertTrue(new SimplePrincipal("test").equals(new SimplePrincipal(
            "test")));
    }
}

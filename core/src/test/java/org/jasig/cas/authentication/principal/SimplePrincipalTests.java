/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import org.apache.commons.lang.builder.ToStringBuilder;

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

    public void testToString() {
        final String id = "test";
        final SimplePrincipal principal = new SimplePrincipal(id);
        assertEquals(ToStringBuilder.reflectionToString(principal), principal
            .toString());
    }
}

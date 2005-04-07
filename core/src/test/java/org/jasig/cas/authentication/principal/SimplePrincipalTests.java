/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import org.apache.commons.lang.builder.ToStringBuilder;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Id: SimplePrincipalTests.java,v 1.3 2005/02/27 05:49:26 sbattaglia
 * Exp $
 */
public class SimplePrincipalTests extends TestCase {

    public void testNullId() {
        try {
            new SimplePrincipal(null);
        } catch (IllegalArgumentException e) {
            return;
        }

        fail("IllegalArgumentException expected.");
    }

    public void testProperId() {
        final String id = "test";
        SimplePrincipal principal = new SimplePrincipal(id);

        assertEquals(id, principal.getId());
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
        SimplePrincipal principal = new SimplePrincipal(id);

        assertEquals(ToStringBuilder.reflectionToString(principal), principal
            .toString());
    }
}
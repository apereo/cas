/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public class SimplePrincipalTests extends TestCase {

    public void testNullId() {
        try {
            new SimplePrincipal(null);
        }
        catch (IllegalArgumentException e) {
            return;
        }

        fail("IllegalArgumentException expected.");
    }

    public void testProperId() {
        final String id = "test";
        SimplePrincipal principal = new SimplePrincipal(id);

        assertEquals(id, principal.getId());
    }
}
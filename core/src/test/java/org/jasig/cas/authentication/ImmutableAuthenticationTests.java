/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public class ImmutableAuthenticationTests extends TestCase {

    private Authentication authentication;

    private Principal principal = new SimplePrincipal("test");

    private Object obj = new Object();

    protected void setUp() throws Exception {
        super.setUp();
        this.authentication = new ImmutableAuthentication(this.principal,
            this.obj);
    }

    public void testGetters() {
        assertEquals(this.authentication.getPrincipal(), this.principal);
        assertEquals(this.authentication.getAttributes(), this.obj);
    }
}

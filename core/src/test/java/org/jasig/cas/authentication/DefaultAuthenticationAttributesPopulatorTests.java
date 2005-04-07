/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import java.util.HashMap;

import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import junit.framework.TestCase;

public class DefaultAuthenticationAttributesPopulatorTests extends TestCase {

    private Authentication authentication;

    private AuthenticationAttributesPopulator populator = new DefaultAuthenticationAttributesPopulator();

    protected void setUp() throws Exception {
        this.authentication = new ImmutableAuthentication(new SimplePrincipal(
            "test"), new HashMap());
        super.setUp();
    }

    public void testPopulateAttributes() {
        assertEquals(this.authentication, this.populator.populateAttributes(
            this.authentication, new UsernamePasswordCredentials()));
    }

}

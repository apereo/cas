/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.bind.support;

import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.web.bind.CredentialsBinder;
import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public class DefaultSpringBindCredentialsBinderTests extends TestCase {

    private CredentialsBinder credentialsBinder = new DefaultSpringBindCredentialsBinder();

    public void testBindUsernamePasswordCredentials() {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUserName("scott");
        c.setPassword("scott");

        this.credentialsBinder.bind(null, c);

        assertEquals("scott", c.getUserName());
        assertEquals("scott", c.getPassword());
    }

    public void testSupportsUsernamePasswordCredentials() {
        assertTrue(this.credentialsBinder.supports(UsernamePasswordCredentials.class));
    }
}

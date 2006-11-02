/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.client;

import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class ServiceTicketCredentialsToPrincipalResolverTests extends TestCase {

    private CredentialsToPrincipalResolver credentialsToPrincipalResolver = new ServiceTicketCredentialsToPrincipalResolver();

    public void testWithAssertion() {
        final Assertion assertion = new AssertionImpl(new SimplePrincipal(
            "test"));
        final ServiceTicketCredentials c = new ServiceTicketCredentials("test");
        c.setAssertion(assertion);
        assertEquals("test", this.credentialsToPrincipalResolver
            .resolvePrincipal(c).getId());
    }

    public void testSupports() {
        assertFalse(this.credentialsToPrincipalResolver.supports(null));
        assertFalse(this.credentialsToPrincipalResolver
            .supports(new UsernamePasswordCredentials()));
        assertTrue(this.credentialsToPrincipalResolver
            .supports(new ServiceTicketCredentials("test")));
    }
}

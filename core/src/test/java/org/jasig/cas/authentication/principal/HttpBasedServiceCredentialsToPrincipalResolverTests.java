/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import java.net.MalformedURLException;
import java.net.URL;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public class HttpBasedServiceCredentialsToPrincipalResolverTests extends TestCase {

    private CredentialsToPrincipalResolver resolver = new HttpBasedServiceCredentialsToPrincipalResolver();

    public void testInValidSupportsCredentials() {
        assertFalse(this.resolver.supports(new UsernamePasswordCredentials()));
    }

    public void testNullSupportsCredentials() {
        assertFalse(this.resolver.supports(null));
    }

    public void testValidSupportsCredentials() {
        try {
            assertTrue(this.resolver.supports(new HttpBasedServiceCredentials(new URL("http://www.rutgers.edu"))));
        }
        catch (MalformedURLException e) {
            fail("Invalid URL supplied.");
        }
    }

    public void testValidCredentials() {
        try {
            HttpBasedServiceCredentials request = new HttpBasedServiceCredentials(new URL("http://www.rutgers.edu"));

            Principal p = this.resolver.resolvePrincipal(request);

            assertEquals(p.getId(), request.getCallbackUrl().toExternalForm());
        }
        catch (MalformedURLException e) {
            fail("Invalid URL supplied.");
        }
    }

    public void testNullCredentials() {
        try {
            this.resolver.resolvePrincipal(null);
        }
        catch (IllegalArgumentException e) {
            return;
        }

        fail("IllegalArgumentException expected.");
    }
}
/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.spnego.authentication.principal;

import org.jasig.cas.adaptors.spnego.MockGssContext;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.5
 *
 */
public class SpnegoCredentialsToPrincipalResolverTests extends TestCase {
    private SpnegoCredentialsToPrincipalResolver resolver;
    
    protected void setUp() throws Exception {
        this.resolver = new SpnegoCredentialsToPrincipalResolver();
    }
    
    public void testValidCredentials() {
        assertEquals("test", this.resolver.resolvePrincipal(new SpnegoCredentials(new MockGssContext(true))).getId());
    }
    
    public void testSupports() {
        assertFalse(this.resolver.supports(null));
        assertTrue(this.resolver.supports(new SpnegoCredentials(new MockGssContext(true))));
        assertFalse(this.resolver.supports(new UsernamePasswordCredentials()));
    }
}

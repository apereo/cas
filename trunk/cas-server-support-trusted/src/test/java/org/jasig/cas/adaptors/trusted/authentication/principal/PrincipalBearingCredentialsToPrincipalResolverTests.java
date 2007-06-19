/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.trusted.authentication.principal;

import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.5
 *
 */
public class PrincipalBearingCredentialsToPrincipalResolverTests extends
    TestCase {
    private PrincipalBearingCredentialsToPrincipalResolver resolver;
    
    public void setUp() throws Exception {
        this.resolver = new PrincipalBearingCredentialsToPrincipalResolver();
    }
    
    public void testSupports() {
        assertTrue(this.resolver.supports(new PrincipalBearingCredentials(new SimplePrincipal("test"))));
        assertFalse(this.resolver.supports(new UsernamePasswordCredentials()));
        assertFalse(this.resolver.supports(null));
    }
    
    public void testReturnedPrincipal() {
        assertEquals("test", this.resolver.resolvePrincipal(new PrincipalBearingCredentials(new SimplePrincipal("test"))).getId());
    }
    
}

/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.trusted.authentication.handler.support;

import org.jasig.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredentials;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import junit.framework.TestCase;

/**
 * @author Andrew Petro
 * @version $Revision$ $Date$
 * @since 3.0.5
 */
public final class PrincipalBearingCredentialsAuthenticationHandlerTests 
    extends TestCase {

    private PrincipalBearingCredentialsAuthenticationHandler handler 
        = new PrincipalBearingCredentialsAuthenticationHandler();    
    /**
     * When the credentials bear a Principal, succeed the authentication.
     */
    public void testNonNullPrincipal() {
        PrincipalBearingCredentials credentials = new PrincipalBearingCredentials(new SimplePrincipal("scott"));
        assertTrue(this.handler.authenticate(credentials));
    }    
    
    public void testSupports() {
        PrincipalBearingCredentials credentials = new PrincipalBearingCredentials(new SimplePrincipal("scott"));
        assertTrue(this.handler.supports(credentials));
        assertFalse(this.handler.supports(new UsernamePasswordCredentials()));
    }
}

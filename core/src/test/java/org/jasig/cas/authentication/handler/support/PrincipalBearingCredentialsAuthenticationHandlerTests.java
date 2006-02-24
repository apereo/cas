/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.PrincipalBearingCredentials;
import org.jasig.cas.authentication.principal.SimplePrincipal;

import junit.framework.TestCase;

/**
 * 
 * @version $Revision$ $Date$
 * @since 3.0.5
 */
public final class PrincipalBearingCredentialsAuthenticationHandlerTests 
    extends TestCase {

    private PrincipalBearingCredentialsAuthenticationHandler handler 
        = new PrincipalBearingCredentialsAuthenticationHandler();
    
    /**
     * When the credentials bear null, fail the authentication.
     * @throws AuthenticationException 
     */
    public void testNullPrincipal() {
        PrincipalBearingCredentials credentials = new PrincipalBearingCredentials();
        // do not set the Principal in the credentials.  it remains null.
        assertFalse(this.handler.authenticate(credentials));
    }
    
    /**
     * When the credentials bear a Principal, succeed the authentication.
     */
    public void testNonNullPrincipal() {
        Principal principal = new SimplePrincipal("scott");
        PrincipalBearingCredentials credentials = new PrincipalBearingCredentials();
        credentials.setPrincipal(principal);
        assertTrue(this.handler.authenticate(credentials));
    }
    
}

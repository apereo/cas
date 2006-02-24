/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.remoteuser.authentication.handler.support;

import org.jasig.cas.adaptors.remoteuser.authentication.handler.support.PrincipalBearingCredentialsAuthenticationHandler;
import org.jasig.cas.adaptors.remoteuser.authentication.principal.PrincipalBearingCredentials;
import org.jasig.cas.authentication.principal.SimplePrincipal;

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
}

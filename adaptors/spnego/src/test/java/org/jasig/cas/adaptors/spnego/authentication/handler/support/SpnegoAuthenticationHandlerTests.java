/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.spnego.authentication.handler.support;

import org.jasig.cas.adaptors.spnego.MockGssContext;
import org.jasig.cas.adaptors.spnego.authentication.principal.SpnegoCredentials;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.5
 *
 */
public class SpnegoAuthenticationHandlerTests extends TestCase {
    private SpnegoAuthenticationHandler authenticationHandler;
    
    protected void setUp() throws Exception {
        this.authenticationHandler = new SpnegoAuthenticationHandler();
    }
    
    public void testSuccessfulAuthentication() throws AuthenticationException {
        final Credentials credentials = new SpnegoCredentials(new MockGssContext(true));
        assertTrue(this.authenticationHandler.authenticate(credentials));
    }
    
    public void testUnsuccessfulAuthentication() throws AuthenticationException {
        final Credentials credentials = new SpnegoCredentials(new MockGssContext(false));
        assertFalse(this.authenticationHandler.authenticate(credentials));
    }
    
    public void testSupports() {
        assertFalse(this.authenticationHandler.supports(null));
        assertTrue(this.authenticationHandler.supports(new SpnegoCredentials(new MockGssContext(true))));
        assertFalse(this.authenticationHandler.supports(new UsernamePasswordCredentials()));
    }
}

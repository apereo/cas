/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.spnego.authentication.handler.support;

import junit.framework.TestCase;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.support.spnego.MockJCSIFAuthentication;
import org.jasig.cas.support.spnego.authentication.handler.support.JCIFSSpnegoAuthenticationHandler;
import org.jasig.cas.support.spnego.authentication.principal.SpnegoCredentials;

/**
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @version $Id$
 * @since 3.1
 * 
 */
public class JCSIFSpnegoAuthenticationHandlerTests extends TestCase {
    private JCIFSSpnegoAuthenticationHandler authenticationHandler;

    protected void setUp() throws Exception {
        this.authenticationHandler = new JCIFSSpnegoAuthenticationHandler();
    }

    public void testSuccessfulAuthenticationWithDomainName() throws AuthenticationException {
        final SpnegoCredentials credentials = new SpnegoCredentials(new byte[] {0, 1, 2});
        this.authenticationHandler.setPrincipalWithDomainName(true);
        this.authenticationHandler.setAuthentication(new MockJCSIFAuthentication(true));
        assertTrue(this.authenticationHandler.authenticate(credentials));
        assertEquals("test", credentials.getPrincipal().getId());
        assertNotNull(credentials.getNextToken());
    }

    public void testSuccessfulAuthenticationWithoutDomainName() throws AuthenticationException {
        final SpnegoCredentials credentials = new SpnegoCredentials(new byte[] {0, 1, 2});
        this.authenticationHandler.setPrincipalWithDomainName(false);
        this.authenticationHandler.setAuthentication(new MockJCSIFAuthentication(true));
        assertTrue(this.authenticationHandler.authenticate(credentials));
        assertEquals("test", credentials.getPrincipal().getId());
        assertNotNull(credentials.getNextToken());
    }

    public void testUnsuccessfulAuthentication() {
        final SpnegoCredentials credentials = new SpnegoCredentials(new byte[] {0, 1, 2});
        this.authenticationHandler.setAuthentication(new MockJCSIFAuthentication(false));
        try {
            this.authenticationHandler.authenticate(credentials);
            fail("An AuthenticationException should have been thrown");
        } catch (AuthenticationException e) {
            assertNull(credentials.getNextToken());
            assertNull(credentials.getPrincipal());
        }
    }

    public void testSupports() {
        assertFalse(this.authenticationHandler.supports(null));
        assertTrue(this.authenticationHandler.supports(new SpnegoCredentials(new byte[] {0, 1, 2})));
        assertFalse(this.authenticationHandler.supports(new UsernamePasswordCredentials()));
    }

    public void testGetSimpleCredentials() {
        String myNtlmUser = "DOMAIN\\Username";
        String myNtlmUserWithNoDomain = "Username";
        String myKerberosUser = "Username@DOMAIN.COM";

        this.authenticationHandler.setPrincipalWithDomainName(true);
        assertEquals(new SimplePrincipal(myNtlmUser), this.authenticationHandler
                .getSimplePrincipal(myNtlmUser, true));
        assertEquals(new SimplePrincipal(myNtlmUserWithNoDomain), this.authenticationHandler
                .getSimplePrincipal(myNtlmUserWithNoDomain, false));
        assertEquals(new SimplePrincipal(myKerberosUser), this.authenticationHandler
                .getSimplePrincipal(myKerberosUser, false));

        this.authenticationHandler.setPrincipalWithDomainName(false);
        assertEquals(new SimplePrincipal("Username"), this.authenticationHandler
                .getSimplePrincipal(myNtlmUser, true));
        assertEquals(new SimplePrincipal("Username"), this.authenticationHandler
                .getSimplePrincipal(myNtlmUserWithNoDomain, true));
        assertEquals(new SimplePrincipal("Username"), this.authenticationHandler
                .getSimplePrincipal(myKerberosUser, false));
    }
}

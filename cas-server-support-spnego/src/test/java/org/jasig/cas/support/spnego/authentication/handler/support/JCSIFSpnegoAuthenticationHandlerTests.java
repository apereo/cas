/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.support.spnego.authentication.handler.support;

import java.security.GeneralSecurityException;

import static org.junit.Assert.*;

import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.support.spnego.MockJCSIFAuthentication;
import org.jasig.cas.support.spnego.authentication.principal.SpnegoCredential;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @since 3.1
 *
 */
public class JCSIFSpnegoAuthenticationHandlerTests {
    private JCIFSSpnegoAuthenticationHandler authenticationHandler;

    @Before
    public void setUp() throws Exception {
        this.authenticationHandler = new JCIFSSpnegoAuthenticationHandler();
    }

    @Test
    public void testSuccessfulAuthenticationWithDomainName() throws Exception {
        final SpnegoCredential credentials = new SpnegoCredential(new byte[] {0, 1, 2});
        this.authenticationHandler.setPrincipalWithDomainName(true);
        this.authenticationHandler.setAuthentication(new MockJCSIFAuthentication(true));
        assertNotNull(this.authenticationHandler.authenticate(credentials));
        assertEquals("test", credentials.getPrincipal().getId());
        assertNotNull(credentials.getNextToken());
    }

    @Test
    public void testSuccessfulAuthenticationWithoutDomainName() throws Exception {
        final SpnegoCredential credentials = new SpnegoCredential(new byte[] {0, 1, 2});
        this.authenticationHandler.setPrincipalWithDomainName(false);
        this.authenticationHandler.setAuthentication(new MockJCSIFAuthentication(true));
        assertNotNull(this.authenticationHandler.authenticate(credentials));
        assertEquals("test", credentials.getPrincipal().getId());
        assertNotNull(credentials.getNextToken());
    }

    @Test
    public void testUnsuccessfulAuthentication() throws Exception {
        final SpnegoCredential credentials = new SpnegoCredential(new byte[] {0, 1, 2});
        this.authenticationHandler.setAuthentication(new MockJCSIFAuthentication(false));
        try {
            this.authenticationHandler.authenticate(credentials);
            fail("An AuthenticationException should have been thrown");
        } catch (final GeneralSecurityException e) {
            assertNull(credentials.getNextToken());
            assertNull(credentials.getPrincipal());
        }
    }

    @Test
    public void testSupports() {
        assertFalse(this.authenticationHandler.supports(null));
        assertTrue(this.authenticationHandler.supports(new SpnegoCredential(new byte[] {0, 1, 2})));
        assertFalse(this.authenticationHandler.supports(new UsernamePasswordCredential()));
    }

    @Test
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

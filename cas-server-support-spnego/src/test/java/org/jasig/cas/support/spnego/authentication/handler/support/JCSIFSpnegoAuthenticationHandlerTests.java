/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.PrincipalFactory;
import org.jasig.cas.support.spnego.MockJCSIFAuthentication;
import org.jasig.cas.support.spnego.authentication.principal.SpnegoCredential;
import org.junit.Before;
import org.junit.Test;

import java.security.GeneralSecurityException;

import static org.junit.Assert.*;

/**
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @since 3.1
 * @deprecated As of 4.1, the class name is abbreviated in a way that is not per camel-casing standards and will be renamed in the future.
 */
@Deprecated
public class JCSIFSpnegoAuthenticationHandlerTests {
    private JCIFSSpnegoAuthenticationHandler authenticationHandler;

    @Before
    public void setUp() throws Exception {
        this.authenticationHandler = new JCIFSSpnegoAuthenticationHandler();
    }

    @Test
    public void verifySuccessfulAuthenticationWithDomainName() throws Exception {
        final SpnegoCredential credentials = new SpnegoCredential(new byte[] {0, 1, 2});
        this.authenticationHandler.setPrincipalWithDomainName(true);
        this.authenticationHandler.setAuthentication(new MockJCSIFAuthentication(true));
        assertNotNull(this.authenticationHandler.authenticate(credentials));
        assertEquals("test", credentials.getPrincipal().getId());
        assertNotNull(credentials.getNextToken());
    }

    @Test
    public void verifySuccessfulAuthenticationWithoutDomainName() throws Exception {
        final SpnegoCredential credentials = new SpnegoCredential(new byte[] {0, 1, 2});
        this.authenticationHandler.setPrincipalWithDomainName(false);
        this.authenticationHandler.setAuthentication(new MockJCSIFAuthentication(true));
        assertNotNull(this.authenticationHandler.authenticate(credentials));
        assertEquals("test", credentials.getPrincipal().getId());
        assertNotNull(credentials.getNextToken());
    }

    @Test
    public void verifyUnsuccessfulAuthentication() throws Exception {
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
    public void verifySupports() {
        assertFalse(this.authenticationHandler.supports(null));
        assertTrue(this.authenticationHandler.supports(new SpnegoCredential(new byte[] {0, 1, 2})));
        assertFalse(this.authenticationHandler.supports(new UsernamePasswordCredential()));
    }

    @Test
    public void verifyGetSimpleCredentials() {
        final String myNtlmUser = "DOMAIN\\Username";
        final String myNtlmUserWithNoDomain = "Username";
        final String myKerberosUser = "Username@DOMAIN.COM";

        final PrincipalFactory factory = new DefaultPrincipalFactory();

        this.authenticationHandler.setPrincipalWithDomainName(true);
        assertEquals(factory.createPrincipal(myNtlmUser), this.authenticationHandler
                .getPrincipal(myNtlmUser, true));
        assertEquals(factory.createPrincipal(myNtlmUserWithNoDomain), this.authenticationHandler
                .getPrincipal(myNtlmUserWithNoDomain, false));
        assertEquals(factory.createPrincipal(myKerberosUser), this.authenticationHandler
                .getPrincipal(myKerberosUser, false));

        this.authenticationHandler.setPrincipalWithDomainName(false);
        assertEquals(factory.createPrincipal("Username"), this.authenticationHandler
                .getPrincipal(myNtlmUser, true));
        assertEquals(factory.createPrincipal("Username"), this.authenticationHandler
                .getPrincipal(myNtlmUserWithNoDomain, true));
        assertEquals(factory.createPrincipal("Username"), this.authenticationHandler
                .getPrincipal(myKerberosUser, false));
    }
}

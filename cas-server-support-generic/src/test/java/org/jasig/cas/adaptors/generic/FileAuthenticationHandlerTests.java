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
package org.jasig.cas.adaptors.generic;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.UnsupportedCredentialsException;
import org.jasig.cas.authentication.HttpBasedServiceCredential;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Scott Battaglia
 */
public class FileAuthenticationHandlerTests  {

    private FileAuthenticationHandler authenticationHandler;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        this.authenticationHandler = new FileAuthenticationHandler();
        this.authenticationHandler.setFileName(
                new ClassPathResource("org/jasig/cas/adaptors/generic/authentication.txt"));

    }

    @Test
    public void testSupportsProperUserCredentials() {
        UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("scott");
        c.setPassword("rutgers");
        try {
            this.authenticationHandler.authenticate(c);
        } catch (UnsupportedCredentialsException e) {
            fail("UnsupportedCredentialsException caught");
        } catch (AuthenticationException e) {
            fail("AuthenticationException caught.");
        }
    }

    @Test
    public void testDoesntSupportBadUserCredentials() {
        try {
            final HttpBasedServiceCredential c = new HttpBasedServiceCredential(
                new URL("http://www.rutgers.edu"));
            assertFalse(this.authenticationHandler.supports(c));
        } catch (MalformedURLException e) {
            fail("MalformedURLException caught.");
        }
    }

    @Test
    public void testAuthenticatesUserInFileWithDefaultSeparator() {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("scott");
        c.setPassword("rutgers");

        try {
            assertTrue(this.authenticationHandler.authenticate(c));
        } catch (AuthenticationException e) {
            fail("AuthenticationException caught but it should not have been thrown.");
        }
    }

    @Test
    public void testFailsUserNotInFileWithDefaultSeparator() throws AuthenticationException {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("fds");
        c.setPassword("rutgers");
        assertFalse(this.authenticationHandler.authenticate(c));
    }

    @Test
    public void testFailsNullUserName() throws AuthenticationException {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername(null);
        c.setPassword("user");
        assertFalse(this.authenticationHandler.authenticate(c));
    }

    @Test
    public void testFailsNullUserNameAndPassword() throws AuthenticationException {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername(null);
        c.setPassword(null);
        assertFalse(this.authenticationHandler.authenticate(c));
    }

    @Test
    public void testFailsNullPassword() throws AuthenticationException {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("scott");
        c.setPassword(null);

        assertFalse(this.authenticationHandler.authenticate(c));
    }

    @Test
    public void testAuthenticatesUserInFileWithCommaSeparator() {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        this.authenticationHandler.setFileName(
                new ClassPathResource("org/jasig/cas/adaptors/generic/authentication2.txt"));
        this.authenticationHandler.setSeparator(",");

        c.setUsername("scott");
        c.setPassword("rutgers");

        try {
            assertTrue(this.authenticationHandler.authenticate(c));
        } catch (AuthenticationException e) {
            fail("AuthenticationException caught but it should not have been thrown.");
        }
    }

    @Test
    public void testFailsUserNotInFileWithCommaSeparator() throws AuthenticationException {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        this.authenticationHandler.setFileName(
                new ClassPathResource("org/jasig/cas/adaptors/generic/authentication2.txt"));
        this.authenticationHandler.setSeparator(",");

        c.setUsername("fds");
        c.setPassword("rutgers");
        assertFalse(this.authenticationHandler.authenticate(c));
    }

    @Test
    public void testFailsGoodUsernameBadPassword() throws AuthenticationException {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        this.authenticationHandler.setFileName(
                new ClassPathResource("org/jasig/cas/adaptors/generic/authentication2.txt"));
        this.authenticationHandler.setSeparator(",");

        c.setUsername("scott");
        c.setPassword("rutgers1");

        assertFalse(this.authenticationHandler.authenticate(c));
    }

    @Test
    public void testAuthenticateNoFileName() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();
        this.authenticationHandler.setFileName(new ClassPathResource("fff"));

        c.setUsername("scott");
        c.setPassword("rutgers");

        assertFalse(this.authenticationHandler.authenticate(c));
    }
}
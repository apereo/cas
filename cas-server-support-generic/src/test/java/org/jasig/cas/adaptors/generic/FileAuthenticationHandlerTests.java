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

import java.net.MalformedURLException;
import java.net.URL;
import junit.framework.TestCase;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.UnsupportedCredentialException;
import org.jasig.cas.authentication.service.HttpBasedServiceCredential;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Scott Battaglia
 * @version $Id: FileAuthenticationHandlerTests.java,v 1.3 2005/02/27 05:49:26
 * sbattaglia Exp $
 */
public class FileAuthenticationHandlerTests extends TestCase {

    private FileAuthenticationHandler authenticationHandler;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.authenticationHandler = new FileAuthenticationHandler();
        this.authenticationHandler.setFileName(new ClassPathResource("org/jasig/cas/adaptors/generic/authentication.txt"));

    }

    public void testSupportsProperUserCredentials() {
        UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("scott");
        c.setPassword("rutgers");
        try {
            this.authenticationHandler.authenticate(c);
        } catch (UnsupportedCredentialException e) {
            fail("UnsupportedCredentialException caught");
        } catch (AuthenticationException e) {
            fail("AuthenticationException caught.");
        }
    }

    public void testDoesntSupportBadUserCredentials() {
        try {
            final HttpBasedServiceCredential c = new HttpBasedServiceCredential(
                new URL("http://www.rutgers.edu"));
            assertFalse(this.authenticationHandler.supports(c));
        } catch (MalformedURLException e) {
            fail("MalformedURLException caught.");
        }
    }

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

    public void testFailsUserNotInFileWithDefaultSeparator() {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("fds");
        c.setPassword("rutgers");

        try {
            assertFalse(this.authenticationHandler.authenticate(c));
        } catch (AuthenticationException e) {
            // this is okay because it means the test failed.
        }
    }

    public void testFailsNullUserName() {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername(null);
        c.setPassword("user");

        try {
            assertFalse(this.authenticationHandler.authenticate(c));
        } catch (AuthenticationException e) {
            // this is okay because it means the test failed.
        }
    }

    public void testFailsNullUserNameAndPassword() {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername(null);
        c.setPassword(null);

        try {
            assertFalse(this.authenticationHandler.authenticate(c));
        } catch (AuthenticationException e) {
            // this is okay because it means the test failed.
        }
    }

    public void testFailsNullPassword() {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("scott");
        c.setPassword(null);

        try {
            assertFalse(this.authenticationHandler.authenticate(c));
        } catch (AuthenticationException e) {
            // this is okay because it means the test failed.
        }
    }

    public void testAuthenticatesUserInFileWithCommaSeparator() {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        this.authenticationHandler.setFileName(new ClassPathResource("org/jasig/cas/adaptors/generic/authentication2.txt"));
        this.authenticationHandler.setSeparator(",");

        c.setUsername("scott");
        c.setPassword("rutgers");

        try {
            assertTrue(this.authenticationHandler.authenticate(c));
        } catch (AuthenticationException e) {
            fail("AuthenticationException caught but it should not have been thrown.");
        }
    }

    public void testFailsUserNotInFileWithCommaSeparator() {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        this.authenticationHandler.setFileName(new ClassPathResource("org/jasig/cas/adaptors/generic/authentication2.txt"));
        this.authenticationHandler.setSeparator(",");

        c.setUsername("fds");
        c.setPassword("rutgers");

        try {
            assertFalse(this.authenticationHandler.authenticate(c));
        } catch (AuthenticationException e) {
            // this is okay because it means the test failed.
        }
    }

    public void testFailsGoodUsernameBadPassword() {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        this.authenticationHandler.setFileName(new ClassPathResource("org/jasig/cas/adaptors/generic/authentication2.txt"));
        this.authenticationHandler.setSeparator(",");

        c.setUsername("scott");
        c.setPassword("rutgers1");

        try {
            assertFalse(this.authenticationHandler.authenticate(c));
        } catch (AuthenticationException e) {
            // this is okay because it means the test failed.
        }
    }

    public void testAuthenticateNoFileName() {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();
        this.authenticationHandler.setFileName(new ClassPathResource("fff"));

        c.setUsername("scott");
        c.setPassword("rutgers");

        try {
            assertFalse(this.authenticationHandler.authenticate(c));
        } catch (Exception e) {
            // this is good
        }
    }
}
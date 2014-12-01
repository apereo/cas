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
package org.jasig.cas.adaptors.generic;

import org.jasig.cas.authentication.HttpBasedServiceCredential;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class FileAuthenticationHandlerTests  {

    private FileAuthenticationHandler authenticationHandler;

    @Before
    public void setUp() throws Exception {
        this.authenticationHandler = new FileAuthenticationHandler();
        this.authenticationHandler.setFileName(
                new ClassPathResource("org/jasig/cas/adaptors/generic/authentication.txt"));

    }

    @Test
    public void verifySupportsProperUserCredentials() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("scott");
        c.setPassword("rutgers");
        assertNotNull(this.authenticationHandler.authenticate(c));
    }

    @Test
    public void verifyDoesntSupportBadUserCredentials() {
        try {
            final RegisteredServiceImpl svc = new RegisteredServiceImpl();
            svc.setServiceId("https://some.app.edu");
            final HttpBasedServiceCredential c = new HttpBasedServiceCredential(
                new URL("http://www.rutgers.edu"), svc);
            assertFalse(this.authenticationHandler.supports(c));
        } catch (final MalformedURLException e) {
            fail("MalformedURLException caught.");
        }
    }

    @Test
    public void verifyAuthenticatesUserInFileWithDefaultSeparator() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("scott");
        c.setPassword("rutgers");

        assertNotNull(this.authenticationHandler.authenticate(c));
    }

    @Test(expected = AccountNotFoundException.class)
    public void verifyFailsUserNotInFileWithDefaultSeparator() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("fds");
        c.setPassword("rutgers");
        this.authenticationHandler.authenticate(c);
    }

    @Test(expected = AccountNotFoundException.class)
    public void verifyFailsNullUserName() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername(null);
        c.setPassword("user");
        this.authenticationHandler.authenticate(c);
    }

    @Test(expected = AccountNotFoundException.class)
    public void verifyFailsNullUserNameAndPassword() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername(null);
        c.setPassword(null);
        this.authenticationHandler.authenticate(c);
    }

    @Test(expected = FailedLoginException.class)
    public void verifyFailsNullPassword() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("scott");
        c.setPassword(null);

        this.authenticationHandler.authenticate(c);
    }

    @Test
    public void verifyAuthenticatesUserInFileWithCommaSeparator() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        this.authenticationHandler.setFileName(
                new ClassPathResource("org/jasig/cas/adaptors/generic/authentication2.txt"));
        this.authenticationHandler.setSeparator(",");

        c.setUsername("scott");
        c.setPassword("rutgers");

        assertNotNull(this.authenticationHandler.authenticate(c));
    }

    @Test(expected = AccountNotFoundException.class)
    public void verifyFailsUserNotInFileWithCommaSeparator() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        this.authenticationHandler.setFileName(
                new ClassPathResource("org/jasig/cas/adaptors/generic/authentication2.txt"));
        this.authenticationHandler.setSeparator(",");

        c.setUsername("fds");
        c.setPassword("rutgers");
        this.authenticationHandler.authenticate(c);
    }

    @Test(expected = FailedLoginException.class)
    public void verifyFailsGoodUsernameBadPassword() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        this.authenticationHandler.setFileName(
                new ClassPathResource("org/jasig/cas/adaptors/generic/authentication2.txt"));
        this.authenticationHandler.setSeparator(",");

        c.setUsername("scott");
        c.setPassword("rutgers1");

        this.authenticationHandler.authenticate(c);
    }

    @Test(expected = PreventedException.class)
    public void verifyAuthenticateNoFileName() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();
        this.authenticationHandler.setFileName(new ClassPathResource("fff"));

        c.setUsername("scott");
        c.setPassword("rutgers");

        this.authenticationHandler.authenticate(c);
    }
}

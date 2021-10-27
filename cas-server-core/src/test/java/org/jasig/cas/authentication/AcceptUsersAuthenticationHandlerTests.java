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
package org.jasig.cas.authentication;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 */
public class AcceptUsersAuthenticationHandlerTests  {

    private final Map<String, String> users;

    private final AcceptUsersAuthenticationHandler authenticationHandler;

    public AcceptUsersAuthenticationHandlerTests() throws Exception {
        this.users = new HashMap<String, String>();

        this.users.put("scott", "rutgers");
        this.users.put("dima", "javarules");
        this.users.put("bill", "thisisAwesoME");
        this.users.put("brian", "t�st");

        this.authenticationHandler = new AcceptUsersAuthenticationHandler();

        this.authenticationHandler.setUsers(this.users);
    }

    @Test
    public void testSupportsSpecialCharacters() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();
        c.setUsername("brian");
        c.setPassword("t�st");
        assertEquals("brian", this.authenticationHandler.authenticate(c).getPrincipal().getId());

    }

    @Test
    public void testSupportsProperUserCredentials() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("scott");
        c.setPassword("rutgers");
       assertTrue(this.authenticationHandler.supports(c));
    }

    @Test
    public void testDoesntSupportBadUserCredentials() {
        try {
            assertFalse(this.authenticationHandler
                    .supports(new HttpBasedServiceCredential(new URL(
                            "http://www.rutgers.edu"))));
        } catch (final MalformedURLException e) {
            fail("Could not resolve URL.");
        }
    }

    @Test
    public void testAuthenticatesUserInMap() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("scott");
        c.setPassword("rutgers");

        try {
            assertEquals("scott", this.authenticationHandler.authenticate(c).getPrincipal().getId());
        } catch (final GeneralSecurityException e) {
            fail("AuthenticationException caught but it should not have been thrown.");
        }
    }

    @Test(expected = AccountNotFoundException.class)
    public void testFailsUserNotInMap() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("fds");
        c.setPassword("rutgers");

        this.authenticationHandler.authenticate(c);
    }

    @Test(expected = AccountNotFoundException.class)
    public void testFailsNullUserName() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername(null);
        c.setPassword("user");

        this.authenticationHandler.authenticate(c);
    }

    @Test(expected = AccountNotFoundException.class)
    public void testFailsNullUserNameAndPassword() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername(null);
        c.setPassword(null);

        this.authenticationHandler.authenticate(c);
    }

    @Test(expected = FailedLoginException.class)
    public void testFailsNullPassword() throws Exception{
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("scott");
        c.setPassword(null);

        this.authenticationHandler.authenticate(c);
    }
}

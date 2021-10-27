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
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;

import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.HttpBasedServiceCredential;
import org.junit.Test;

/**
 * @author Scott Battaglia
 */
public class RejectUsersAuthenticationHandlerTests {

    private final List<String> users;

    private final RejectUsersAuthenticationHandler authenticationHandler;

    public RejectUsersAuthenticationHandlerTests() throws Exception {
        this.users = new ArrayList<String>();

        this.users.add("scott");
        this.users.add("dima");
        this.users.add("bill");

        this.authenticationHandler = new RejectUsersAuthenticationHandler();

        this.authenticationHandler.setUsers(this.users);
    }

    @Test
    public void testSupportsProperUserCredentials() throws Exception {
        UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("fff");
        c.setPassword("rutgers");
        assertNotNull(this.authenticationHandler.authenticate(c));
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

    @Test(expected=FailedLoginException.class)
    public void testFailsUserInMap() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("scott");
        c.setPassword("rutgers");
        this.authenticationHandler.authenticate(c);
    }

    @Test
    public void testPassesUserNotInMap() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("fds");
        c.setPassword("rutgers");

        assertNotNull(this.authenticationHandler.authenticate(c));
    }

    @Test(expected = AccountNotFoundException.class)
    public void testPassesNullUserName() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername(null);
        c.setPassword("user");

        this.authenticationHandler.authenticate(c);
    }

    @Test(expected = AccountNotFoundException.class)
    public void testPassesNullUserNameAndPassword() throws Exception {
        this.authenticationHandler.authenticate(new UsernamePasswordCredential());
    }
}

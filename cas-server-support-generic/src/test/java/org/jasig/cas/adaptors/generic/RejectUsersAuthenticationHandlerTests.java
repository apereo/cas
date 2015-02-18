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
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.junit.Test;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class RejectUsersAuthenticationHandlerTests {

    private final List<String> users;

    private final RejectUsersAuthenticationHandler authenticationHandler;

    public RejectUsersAuthenticationHandlerTests() throws Exception {
        this.users = new ArrayList<>();

        this.users.add("scott");
        this.users.add("dima");
        this.users.add("bill");

        this.authenticationHandler = new RejectUsersAuthenticationHandler();

        this.authenticationHandler.setUsers(this.users);
    }

    @Test
    public void verifySupportsProperUserCredentials() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("fff");
        c.setPassword("rutgers");
        assertNotNull(this.authenticationHandler.authenticate(c));
    }

    @Test
    public void verifyDoesntSupportBadUserCredentials() {
        try {
            final RegisteredServiceImpl svc = new RegisteredServiceImpl();
            svc.setServiceId("https://some.app.edu");
            assertFalse(this.authenticationHandler
                .supports(new HttpBasedServiceCredential(new URL(
                    "http://www.rutgers.edu"), svc)));
        } catch (final MalformedURLException e) {
            fail("Could not resolve URL.");
        }
    }

    @Test(expected=FailedLoginException.class)
    public void verifyFailsUserInMap() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("scott");
        c.setPassword("rutgers");
        this.authenticationHandler.authenticate(c);
    }

    @Test
    public void verifyPassesUserNotInMap() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("fds");
        c.setPassword("rutgers");

        assertNotNull(this.authenticationHandler.authenticate(c));
    }

    @Test(expected = AccountNotFoundException.class)
    public void verifyPassesNullUserName() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername(null);
        c.setPassword("user");

        this.authenticationHandler.authenticate(c);
    }

    @Test(expected = AccountNotFoundException.class)
    public void verifyPassesNullUserNameAndPassword() throws Exception {
        this.authenticationHandler.authenticate(new UsernamePasswordCredential());
    }
}

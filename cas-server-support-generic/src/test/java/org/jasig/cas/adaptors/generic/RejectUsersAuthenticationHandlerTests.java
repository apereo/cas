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
import java.util.ArrayList;
import java.util.List;

import org.jasig.cas.adaptors.generic.RejectUsersAuthenticationHandler;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 */
public class RejectUsersAuthenticationHandlerTests extends TestCase {

    final private List<String> users;

    final private RejectUsersAuthenticationHandler authenticationHandler;

    public RejectUsersAuthenticationHandlerTests() throws Exception {
        this.users = new ArrayList<String>();

        this.users.add("scott");
        this.users.add("dima");
        this.users.add("bill");

        this.authenticationHandler = new RejectUsersAuthenticationHandler();

        this.authenticationHandler.setUsers(this.users);
    }

    public void testSupportsProperUserCredentials() {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUsername("fff");
        c.setPassword("rutgers");
        try {
            this.authenticationHandler.authenticate(c);
        } catch (AuthenticationException e) {
            fail("AuthenticationException caught.");
        }
    }

    public void testDoesntSupportBadUserCredentials() {
        try {
            assertFalse(this.authenticationHandler
                .supports(new HttpBasedServiceCredentials(new URL(
                    "http://www.rutgers.edu"))));
        } catch (MalformedURLException e) {
            fail("Could not resolve URL.");
        }
    }

    public void testFailsUserInMap() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUsername("scott");
        c.setPassword("rutgers");

        try {
            assertFalse(this.authenticationHandler.authenticate(c));
        } catch (AuthenticationException e) {
            // fail("AuthenticationException caught but it should not have been
            // thrown.");
        }
    }

    public void testPassesUserNotInMap() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUsername("fds");
        c.setPassword("rutgers");

        try {
            assertTrue(this.authenticationHandler.authenticate(c));
        } catch (AuthenticationException e) {
            fail("Exception thrown but not expected.");
        }
    }

    public void testFailsNullUserName() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUsername(null);
        c.setPassword("user");

        try {
            assertTrue(this.authenticationHandler.authenticate(c));
        } catch (AuthenticationException e) {
            fail("Exception expected as null should never be in map.");
        }
    }

    public void testFailsNullUserNameAndPassword() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUsername(null);
        c.setPassword(null);

        try {
            assertTrue(this.authenticationHandler.authenticate(c));
        } catch (AuthenticationException e) {
            fail("Exception expected as null should never be in map.");
        }
    }
}
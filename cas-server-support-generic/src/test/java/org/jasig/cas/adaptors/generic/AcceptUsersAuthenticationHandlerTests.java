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
import java.util.HashMap;
import java.util.Map;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.service.HttpBasedServiceCredential;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 */
public class AcceptUsersAuthenticationHandlerTests extends TestCase {

    final private Map<String, String> users;

    final private AcceptUsersAuthenticationHandler authenticationHandler;

    public AcceptUsersAuthenticationHandlerTests() throws Exception {
        this.users = new HashMap<String, String>();

        this.users.put("scott", "rutgers");
        this.users.put("dima", "javarules");
        this.users.put("bill", "thisisAwesoME");
        this.users.put("brian", "t�st");

        this.authenticationHandler = new AcceptUsersAuthenticationHandler();

        this.authenticationHandler.setUsers(this.users);
    }
    
    public void testSupportsSpecialCharacters() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();
        c.setUsername("brian");
        c.setPassword("t�st");
        assertTrue(this.authenticationHandler.authenticate(c));

    }

    public void testSupportsProperUserCredentials() throws Exception {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("scott");
        c.setPassword("rutgers");
        this.authenticationHandler.authenticate(c);
    }

    public void testDoesntSupportBadUserCredentials() {
        try {
            assertFalse(this.authenticationHandler
                .supports(new HttpBasedServiceCredential(new URL(
                    "http://www.rutgers.edu"))));
        } catch (MalformedURLException e) {
            fail("Could not resolve URL.");
        }
    }

    public void testAuthenticatesUserInMap() {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();

        c.setUsername("scott");
        c.setPassword("rutgers");

        try {
            assertTrue(this.authenticationHandler.authenticate(c));
        } catch (AuthenticationException e) {
            fail("AuthenticationException caught but it should not have been thrown.");
        }
    }

    public void testFailsUserNotInMap() {
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
}
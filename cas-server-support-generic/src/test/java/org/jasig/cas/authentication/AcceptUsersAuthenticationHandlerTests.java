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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link AcceptUsersAuthenticationHandler}.
 *
 * @author  Marvin S. Addison
 */
@RunWith(Parameterized.class)
public class AcceptUsersAuthenticationHandlerTests {

    private final AcceptUsersAuthenticationHandler authenticationHandler;

    private final UsernamePasswordCredential credential;

    boolean expected;

    public AcceptUsersAuthenticationHandlerTests(
            final AcceptUsersAuthenticationHandler handler,
            final UsernamePasswordCredential credential,
            final boolean expected) {
        this.authenticationHandler = handler;
        this.credential = credential;
        this.expected = expected;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> generateData() throws Exception {
        final Map<String, String> whitelist = new HashMap<String, String>();
        whitelist.put("groucho", "password");
        whitelist.put("harpo", "password");
        whitelist.put("chico", "password");
        whitelist.put("zeppo", "password");

        final AcceptUsersAuthenticationHandler handler = new AcceptUsersAuthenticationHandler();
        handler.setUsers(whitelist);

        return Arrays.asList(new Object[][]{
                { handler, newCredential("groucho", "password"), true },
                { handler, newCredential("groucho", "PassWord"), false },
                { handler, newCredential("groucho", "invalid"), false },
                { handler, newCredential("nobody", "invalid"), false },
                { handler, newCredential("nobody", null), false },
                { handler, newCredential(null, null), false },
        });
    }

    @Test
    public void testAuthenticate() {
        boolean success;
        try {
            this.authenticationHandler.authenticate(this.credential);
            success = true;
        } catch (Exception e) {
            success = false;
        }
        assertEquals(this.expected, success);
    }



    private static UsernamePasswordCredential newCredential(final String user, final String password) {
        final UsernamePasswordCredential credential = new UsernamePasswordCredential();
        credential.setUsername(user);
        credential.setPassword(password);
        return credential;
    }
}
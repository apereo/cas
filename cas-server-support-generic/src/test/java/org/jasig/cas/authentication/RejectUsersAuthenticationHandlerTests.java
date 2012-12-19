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
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link RejectUsersAuthenticationHandler}.
 *
 * @author Marvin S. Addison
 */
@RunWith(Parameterized.class)
public class RejectUsersAuthenticationHandlerTests {

    private final RejectUsersAuthenticationHandler authenticationHandler;

    private final String username;

    boolean expected;

    public RejectUsersAuthenticationHandlerTests(
            final RejectUsersAuthenticationHandler handler, final String username, final boolean expected) {
        this.authenticationHandler = handler;
        this.username = username;
        this.expected = expected;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> generateData() throws Exception {
        final List<String> blacklist = Arrays.asList("groucho", "harpo", "chico", "zeppo");
        final RejectUsersAuthenticationHandler handler = new RejectUsersAuthenticationHandler();
        handler.setUsers(blacklist);
        return Arrays.asList(new Object[][]{
                {handler, "groucho", false},
                {handler, "socrates", true},
        });
    }

    @Test
    public void testAuthenticate() {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();
        c.setUsername(this.username);
        c.setPassword("NOTUSED");
        boolean success;
        try {
            this.authenticationHandler.authenticate(c);
            success = true;
        } catch (Exception e) {
            success = false;
        }
        assertEquals(this.expected, success);
    }

}
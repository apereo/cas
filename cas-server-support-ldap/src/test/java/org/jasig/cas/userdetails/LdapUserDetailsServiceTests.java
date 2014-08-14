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
package org.jasig.cas.userdetails;

import org.jasig.cas.authentication.AbstractLdapTests;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ldaptive.Connection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;

/**
 * Unit test for the {@link LdapUserDetailsService} class.
 * <p>
 * The virginiaTechGroup schema MUST be installed on the target directories prior to running this test.
 *
 * @author Marvin Addison
 */
@RunWith(Parameterized.class)
public class LdapUserDetailsServiceTests extends AbstractLdapTests {

    private LdapUserDetailsService userDetailsService;

    public LdapUserDetailsServiceTests(final String ... contextPaths) {
        this.contextPaths = contextPaths;
    }

    @Parameters
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(new Object[][]{{new String[]{"/ldap-context.xml", "/openldap-userdetails-test.xml"}}});
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.userDetailsService = this.context.getBean(LdapUserDetailsService.class);
    }

    @Test
    public void testLoadUserByUsername() throws Exception {
        final Connection c = super.getConnection();
        /*
        for (final LdapEntry entry : this.testEntries) {
            final String username = getUsername(entry);
            final UserDetails user = userDetailsService.loadUserByUsername(username);
            assertEquals(username, user.getUsername());
            assertTrue(hasAuthority(user, "ROLE_ADMINISTRATORS"));
            assertTrue(hasAuthority(user, "ROLE_USERS"));
        }
        */
    }

    private boolean hasAuthority(final UserDetails user, final String name) {
        for (final GrantedAuthority authority : user.getAuthorities()) {
            if (authority.getAuthority().equals(name)) {
                return true;
            }
        }
        return false;
    }
}

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

import java.util.Arrays;
import java.util.Collection;

import org.jasig.cas.authentication.AbstractLdapTests;
import org.jasig.cas.util.LdapTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ldaptive.Connection;
import org.ldaptive.LdapEntry;
import org.springframework.core.io.Resource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for the {@link LdapUserDetailsService} class.
 * <p>
 * The virginiaTechGroup schema MUST be installed on the target directories prior to running this test.
 *
 * @author Marvin Addison
 */
@RunWith(Parameterized.class)
public class LdapUserDetailsServiceTests extends AbstractLdapTests {

    private Resource groupsLdif;

    private Collection<LdapEntry> groupEntries;

    private LdapUserDetailsService userDetailsService;

    public LdapUserDetailsServiceTests(
            final LdapTestUtils.DirectoryType directoryType, final String ... contextPaths) {

        this.directoryType = directoryType;
        this.contextPaths = contextPaths;
    }

    @Parameters
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(new Object[][]{
                {
                        LdapTestUtils.DirectoryType.OpenLdap,
                        new String[]{"/ldap-provision-context.xml", "/openldap-userdetails-test.xml"},
                },
        });
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.groupsLdif = context.getBean("groupsLdif", Resource.class);
        this.groupEntries = LdapTestUtils.readLdif(this.groupsLdif, this.baseDn);
        final Connection connection = getConnection();
        try {
            connection.open();
            LdapTestUtils.createLdapEntries(connection, this.directoryType, this.groupEntries);
        } finally {
            connection.close();
        }
        this.userDetailsService = this.context.getBean(LdapUserDetailsService.class);
    }

    @Test
    public void testLoadUserByUsername() throws Exception {
        UserDetails user;
        String username;
        for (final LdapEntry entry : this.testEntries) {
            username = getUsername(entry);
            user = userDetailsService.loadUserByUsername(username);
            assertEquals(username, user.getUsername());
            assertTrue(hasAuthority(user, "ROLE_ADMINISTRATORS"));
            assertTrue(hasAuthority(user, "ROLE_USERS"));
        }
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        if (!this.enableLdapTests) {
            return;
        }
        final Connection connection = getConnection();
        try {
            connection.open();
            LdapTestUtils.removeLdapEntries(connection, this.groupEntries);
        } finally {
            connection.close();
        }
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

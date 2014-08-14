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

import org.jasig.cas.util.LdapTestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ldaptive.LdapEntry;

import javax.security.auth.login.FailedLoginException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Unit test for {@link LdapAuthenticationHandler}.
 *
 * @author Marvin S. Addison
 */
@RunWith(Parameterized.class)
public class LdapAuthenticationHandlerTests extends AbstractLdapTests {

    private LdapAuthenticationHandler handler;

    private boolean supportsNotFound;

    public LdapAuthenticationHandlerTests(
            final LdapTestUtils.DirectoryType directoryType,
            final boolean supportsNotFound,
            final String ... contextPaths) {

        this.directoryType = directoryType;
        this.supportsNotFound = supportsNotFound;
        this.contextPaths = contextPaths;
    }

    @Parameters
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                {
                        LdapTestUtils.DirectoryType.ActiveDirectory,
                        false,
                        new String[] {"/ldap-provision-context.xml", "/ad-authn-test.xml"},
                },
                /*
                {
                        LdapTestUtils.DirectoryType.OpenLdap,
                        true,
                        new String[] {"/ldap-provision-context.xml", "/openldap-searchbind-authn-test.xml"},
                },
                {
                        LdapTestUtils.DirectoryType.OpenLdap,
                        true,
                        new String[] {"/ldap-provision-context.xml", "/openldap-anonsearchbind-authn-test.xml"},
                },
                {
                        LdapTestUtils.DirectoryType.OpenLdap,
                        false,
                        new String[] {"/ldap-provision-context.xml", "/openldap-directbind-authn-test.xml"},
                }
                */
        });
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.handler = this.context.getBean(LdapAuthenticationHandler.class);
    }
    @Test
    public void testAuthenticateSuccess() throws Exception {
        String username;
        for (final LdapEntry entry : this.testEntries) {
            username = getUsername(entry);
            final HandlerResult result = this.handler.authenticate(
                    new UsernamePasswordCredential(username,
                            entry.getAttribute("userPassword").getStringValue()));
            assertNotNull(result.getPrincipal());
            assertEquals(username, result.getPrincipal().getId());
            assertEquals(
                    entry.getAttribute("displayName").getStringValue(),
                    result.getPrincipal().getAttributes().get("displayName"));
            assertEquals(
                    entry.getAttribute("mail").getStringValue(),
                    result.getPrincipal().getAttributes().get("mail"));
        }
    }

    @Test(expected=FailedLoginException.class)
    public void testAuthenticateFailure() throws Exception {
        for (final LdapEntry entry : this.testEntries) {
            final String username = getUsername(entry);
            this.handler.authenticate(new UsernamePasswordCredential(username, "badpassword"));
            fail("Should have thrown FailedLoginException.");

        }
    }

    @Test(expected=FailedLoginException.class)
    public void testAuthenticateNotFound() throws Exception {
        this.handler.authenticate(new UsernamePasswordCredential("notfound", "somepwd"));
        fail("Should have thrown FailedLoginException.");
    }
}

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
package org.jasig.cas.persondir;

import java.util.Arrays;
import java.util.Collection;

import org.jasig.cas.authentication.AbstractLdapTests;
import org.jasig.cas.util.LdapTestUtils;
import org.jasig.services.persondir.IPersonAttributes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.ldaptive.LdapEntry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link LdapPersonAttributeDao}.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
@RunWith(Parameterized.class)
public class LdapPersonAttributeDaoTests extends AbstractLdapTests {

    private LdapPersonAttributeDao attributeDao;

    public LdapPersonAttributeDaoTests(
            final LdapTestUtils.DirectoryType directoryType, final String ... contextPaths) {

        this.directoryType = directoryType;
        this.contextPaths = contextPaths;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(new Object[][]{
                {
                        LdapTestUtils.DirectoryType.OpenLdap,
                        new String[]{"/ldap-provision-context.xml", "/openldap-persondir-test.xml"},
                },
        });
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.attributeDao = this.context.getBean(LdapPersonAttributeDao.class);
    }

    @Test
    public void testGetPerson() throws Exception {
        IPersonAttributes actual;
        String username;
        for (final LdapEntry entry : this.testEntries) {
            username = getUsername(entry);
            actual = attributeDao.getPerson(username);
            assertNotNull(actual);
            assertEquals(username, actual.getName());
            assertSameValues(entry.getAttribute("mail").getStringValues(), actual.getAttributes().get("mail"));
        }
    }

    /**
     * Determines whether the given attribute maps are equal irrespective of value ordering.
     *
     * @param a Expected attribute values.
     * @param b Actual attribute values.
     */
    private static void assertSameValues(final Collection<String> a, final Collection<Object> b) {
        assertEquals(a.size(), b.size());
        for (final String item : a) {
            assertTrue(b.contains(item));
        }
    }
}

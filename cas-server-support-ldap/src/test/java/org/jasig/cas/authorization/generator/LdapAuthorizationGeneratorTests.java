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
package org.jasig.cas.authorization.generator;

import org.jasig.cas.adaptors.ldap.AbstractLdapTests;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldaptive.LdapEntry;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Unit test for the {@link LdapAuthorizationGenerator} class.
 * <p>
 * The virginiaTechGroup schema MUST be installed on the target directories prior to running this test.
 *
 * @author Marvin Addison
 * @since 4.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/ldap-context.xml", "/ldap-authorizationgenerator-test.xml"})
public class LdapAuthorizationGeneratorTests extends AbstractLdapTests {

    private static final String CAS_SERVICE_DETAILS_OBJ_CLASS = "casServiceUserDetails";

    @Autowired
    @Qualifier("ldapAuthorizationGenerator")
    private LdapAuthorizationGenerator ldapAuthorizationGenerator;

    @BeforeClass
    public static void bootstrap() throws Exception {
        initDirectoryServer();
    }

    @Test
    public void verifyLoadUserByUsername() throws Exception {
        for (final LdapEntry entry : getEntries()) {

            if (entry.getAttribute("objectclass").getStringValues().contains(CAS_SERVICE_DETAILS_OBJ_CLASS)) {
                final String username = getUsername(entry);
                final CommonProfile profile = new CommonProfile();
                profile.setId(username);
                ldapAuthorizationGenerator.generate(profile);
                assertTrue(hasAuthority(profile, "ROLE_ADMINISTRATORS"));
                assertTrue(hasAuthority(profile, "ROLE_USERS"));
            }
        }
    }

    private boolean hasAuthority(final CommonProfile profile, final String name) {
        return profile.getRoles().contains(name);
    }
}

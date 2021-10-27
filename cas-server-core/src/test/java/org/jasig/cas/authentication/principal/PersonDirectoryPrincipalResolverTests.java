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

package org.jasig.cas.authentication.principal;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.Credential;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link PersonDirectoryPrincipalResolver}.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class PersonDirectoryPrincipalResolverTests {

    @Test
    public void verifyNullPrincipal() {
        final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
        final Principal p = resolver.resolve(new Credential() {
            @Override
            public String getId() {
                return null;
            }
        });
        assertNull(p);

    }

    @Test
    public void verifyNullAttributes() {
        final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
        resolver.setReturnNullIfNoAttributes(true);
        resolver.setPrincipalAttributeName(TestUtils.CONST_USERNAME);
        final Credential c = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final Principal p = resolver.resolve(c);
        assertNull(p);
    }

    @Test
    public void verifyNoAttributesWithPrincipal() {
        final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
        resolver.setPrincipalAttributeName(TestUtils.CONST_USERNAME);
        final Credential c = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final Principal p = resolver.resolve(c);
        assertNotNull(p);
    }

    @Test
    public void verifyAttributesWithPrincipal() {
        final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
        resolver.setAttributeRepository(TestUtils.getAttributeRepository());
        resolver.setPrincipalAttributeName("cn");
        final Credential c = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final Principal p = resolver.resolve(c);
        assertNotNull(p);
        assertNotEquals(p.getId(), TestUtils.CONST_USERNAME);
        assertTrue(p.getAttributes().containsKey("memberOf"));
    }

}

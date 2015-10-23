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

package org.jasig.cas.authentication;

import org.jasig.cas.TestUtils;
import org.junit.Test;


import static org.junit.Assert.*;

/**
 * Tests for {@link org.jasig.cas.authentication.CacheCredentialsMetaDataPopulator}.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class CacheCredentialsMetaDataPopulatorTests {

    @Test
    public void verifyPasswordAsAuthenticationAttribute() {
        final CacheCredentialsMetaDataPopulator populator = new CacheCredentialsMetaDataPopulator();

        final UsernamePasswordCredential c = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final AuthenticationBuilder builder = DefaultAuthenticationBuilder.newInstance(TestUtils.getAuthentication());
        populator.populateAttributes(builder, c);
        final Authentication authn = builder.build();
        assertTrue(authn.getAttributes().containsKey(UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD));
        assertTrue(authn.getAttributes().get(UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD)
                .equals(c.getPassword()));
    }


}

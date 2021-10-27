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

package org.jasig.cas.extension.clearpass;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.DefaultAuthenticationBuilder;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link CacheCredentialsMetaDataPopulator}.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class CacheCredentialsMetaDataPopulatorTests {

    @Test
    public void verifyAttributePopulationWithPassword() {
        final Authentication auth = TestUtils.getAuthentication();
        final Map<String, String> map = new HashMap<>();
        final CacheCredentialsMetaDataPopulator populator = new CacheCredentialsMetaDataPopulator(map);

        final UsernamePasswordCredential c = TestUtils.getCredentialsWithSameUsernameAndPassword();
        populator.populateAttributes(DefaultAuthenticationBuilder.newInstance(auth), c);

        assertTrue(map.containsKey(auth.getPrincipal().getId()));
        assertEquals(map.get(auth.getPrincipal().getId()), c.getPassword());
    }

    @Test
    public void verifyAttributePopulationWithPasswordWithDifferentCredentialsType() {
        final Authentication auth = TestUtils.getAuthentication();
        final Map<String, String> map = new HashMap<>();
        final CacheCredentialsMetaDataPopulator populator = new CacheCredentialsMetaDataPopulator(map);

        final Credential c = new Credential() {
            @Override
            public String getId() {
                return "something";
            }
        };

        if (populator.supports(c)) {
            populator.populateAttributes(DefaultAuthenticationBuilder.newInstance(auth), c);
        }

        assertEquals(map.size(), 0);

    }

}

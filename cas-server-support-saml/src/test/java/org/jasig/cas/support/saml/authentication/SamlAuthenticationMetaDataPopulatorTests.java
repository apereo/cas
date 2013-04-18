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
package org.jasig.cas.support.saml.authentication;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.MutableAuthentication;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Scott Battaglia
 * @since 3.1
 *
 */
public class SamlAuthenticationMetaDataPopulatorTests {

    private SamlAuthenticationMetaDataPopulator populator;

    @Before
    public void setUp() throws Exception {
        this.populator = new SamlAuthenticationMetaDataPopulator();
    }

    @Test
    public void testAuthenticationTypeFound() {
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials();
        final MutableAuthentication ma = new MutableAuthentication(TestUtils.getPrincipal());

        final Authentication m2 = this.populator.populateAttributes(ma, credentials);

        assertEquals(m2.getAttributes().get("samlAuthenticationStatementAuthMethod"),
                SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_PASSWORD);
    }

    @Test
    public void testAuthenticationTypeNotFound() {
        final CustomCredentials credentials = new CustomCredentials();
        final MutableAuthentication ma = new MutableAuthentication(TestUtils.getPrincipal());

        final Authentication m2 = this.populator.populateAttributes(ma, credentials);

        assertNull(m2.getAttributes().get("samlAuthenticationStatementAuthMethod"));
    }

    @Test
    public void testAuthenticationTypeFoundCustom() {
        final CustomCredentials credentials = new CustomCredentials();

        final Map<String, String> added = new HashMap<String, String>();
        added.put(CustomCredentials.class.getName(), "FF");

        this.populator.setUserDefinedMappings(added);

        final MutableAuthentication ma = new MutableAuthentication(TestUtils.getPrincipal());

        final Authentication m2 = this.populator.populateAttributes(ma, credentials);

        assertEquals("FF", m2.getAttributes().get("samlAuthenticationStatementAuthMethod"));
    }

    protected class CustomCredentials implements Credentials {
        private static final long serialVersionUID = -3387599342233073148L;
    }
}

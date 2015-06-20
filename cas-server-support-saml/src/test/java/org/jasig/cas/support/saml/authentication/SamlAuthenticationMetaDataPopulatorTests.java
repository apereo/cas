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
package org.jasig.cas.support.saml.authentication;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.DefaultAuthenticationBuilder;
import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.CredentialMetaData;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.Principal;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
    public void verifyAuthenticationTypeFound() {
        final UsernamePasswordCredential credentials = new UsernamePasswordCredential();
        final DefaultAuthenticationBuilder builder = newAuthenticationBuilder(TestUtils.getPrincipal());
        this.populator.populateAttributes(builder, credentials);
        final Authentication auth = builder.build();

        assertEquals(
                auth.getAttributes().get(SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD),
                SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_PASSWORD);
    }

    @Test
    public void verifyAuthenticationTypeNotFound() {
        final CustomCredential credentials = new CustomCredential();
        final DefaultAuthenticationBuilder builder = newAuthenticationBuilder(TestUtils.getPrincipal());
        this.populator.populateAttributes(builder, credentials);
        final Authentication auth = builder.build();

        assertNull(auth.getAttributes().get(SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD));
    }

    @Test
    public void verifyAuthenticationTypeFoundCustom() {
        final CustomCredential credentials = new CustomCredential();

        final Map<String, String> added = new HashMap<>();
        added.put(CustomCredential.class.getName(), "FF");

        this.populator.setUserDefinedMappings(added);

        final DefaultAuthenticationBuilder builder = newAuthenticationBuilder(TestUtils.getPrincipal());
        this.populator.populateAttributes(builder, credentials);
        final Authentication auth = builder.build();

        assertEquals(
                "FF",
                auth.getAttributes().get(SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD));
    }

    private static class CustomCredential implements Credential {

        public String getId() {
            return "nobody";
        }
    }

    private static DefaultAuthenticationBuilder newAuthenticationBuilder(final Principal principal) {
        final CredentialMetaData meta = new BasicCredentialMetaData(new UsernamePasswordCredential());
        final AuthenticationHandler handler = new SimpleTestUsernamePasswordAuthenticationHandler();
        return new DefaultAuthenticationBuilder(principal)
                .addCredential(meta)
                .addSuccess("test", new DefaultHandlerResult(handler, meta));
    }
}

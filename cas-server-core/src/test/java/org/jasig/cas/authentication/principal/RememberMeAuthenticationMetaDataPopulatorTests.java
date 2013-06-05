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
package org.jasig.cas.authentication.principal;

import static org.junit.Assert.*;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationBuilder;
import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.CredentialMetaData;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.LegacyAuthenticationHandlerAdapter;
import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.junit.Test;

/**
 *
 * @author Scott Battaglia
 * @since 3.2.1
 *
 */
public class RememberMeAuthenticationMetaDataPopulatorTests {

    private RememberMeAuthenticationMetaDataPopulator p  = new RememberMeAuthenticationMetaDataPopulator();

    @Test
    public void testWithTrueRememberMeCredentials() {
        final RememberMeUsernamePasswordCredentials c = new RememberMeUsernamePasswordCredentials();
        c.setRememberMe(true);
        final AuthenticationBuilder builder = newBuilder(c);
        final Authentication auth = builder.build();

        assertEquals(true, auth.getAttributes().get(RememberMeCredentials.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME));
    }

    @Test
    public void testWithFalseRememberMeCredentials() {
        final RememberMeUsernamePasswordCredentials c = new RememberMeUsernamePasswordCredentials();
        c.setRememberMe(false);
        final AuthenticationBuilder builder = newBuilder(c);
        final Authentication auth = builder.build();

        assertNull(auth.getAttributes().get(RememberMeCredentials.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME));
    }

    @Test
    public void testWithoutRememberMeCredentials() {
        final AuthenticationBuilder builder = newBuilder(TestUtils.getCredentialsWithSameUsernameAndPassword());
        final Authentication auth = builder.build();

        assertNull(auth.getAttributes().get(RememberMeCredentials.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME));
    }

    private AuthenticationBuilder newBuilder(final Credentials credentials) {
        final CredentialMetaData meta = new BasicCredentialMetaData(new UsernamePasswordCredentials());
        final AuthenticationHandler handler = new SimpleTestUsernamePasswordAuthenticationHandler();
        final AuthenticationBuilder builder = new AuthenticationBuilder(TestUtils.getPrincipal())
                .addCredential(meta)
                .addSuccess("test", new HandlerResult(handler, meta));

        this.p.populateAttributes(builder, credentials);
        return builder;
    }

}

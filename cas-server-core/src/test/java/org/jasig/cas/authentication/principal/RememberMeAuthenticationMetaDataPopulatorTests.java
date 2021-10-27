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
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationBuilder;
import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.CredentialMetaData;
import org.jasig.cas.authentication.DefaultAuthenticationBuilder;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.RememberMeCredential;
import org.jasig.cas.authentication.RememberMeUsernamePasswordCredential;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 * @author Scott Battaglia
 * @since 3.2.1
 *
 */
public class RememberMeAuthenticationMetaDataPopulatorTests {

    private final RememberMeAuthenticationMetaDataPopulator p  = new RememberMeAuthenticationMetaDataPopulator();

    @Test
    public void verifyWithTrueRememberMeCredentials() {
        final RememberMeUsernamePasswordCredential c = new RememberMeUsernamePasswordCredential();
        c.setRememberMe(true);
        final AuthenticationBuilder builder = newBuilder(c);
        final Authentication auth = builder.build();

        assertEquals(true, auth.getAttributes().get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME));
    }

    @Test
    public void verifyWithFalseRememberMeCredentials() {
        final RememberMeUsernamePasswordCredential c = new RememberMeUsernamePasswordCredential();
        c.setRememberMe(false);
        final AuthenticationBuilder builder = newBuilder(c);
        final Authentication auth = builder.build();

        assertNull(auth.getAttributes().get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME));
    }

    @Test
    public void verifyWithoutRememberMeCredentials() {
        final AuthenticationBuilder builder = newBuilder(TestUtils.getCredentialsWithSameUsernameAndPassword());
        final Authentication auth = builder.build();

        assertNull(auth.getAttributes().get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME));
    }

    private AuthenticationBuilder newBuilder(final Credential credential) {
        final CredentialMetaData meta = new BasicCredentialMetaData(new UsernamePasswordCredential());
        final AuthenticationHandler handler = new SimpleTestUsernamePasswordAuthenticationHandler();
        final AuthenticationBuilder builder = new DefaultAuthenticationBuilder(TestUtils.getPrincipal())
                .addCredential(meta)
                .addSuccess("test", new DefaultHandlerResult(handler, meta));

        if (this.p.supports(credential)) {
            this.p.populateAttributes(builder, credential);
        }
        return builder;
    }

}

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
package org.jasig.cas.support.pac4j.authentication.handler.support;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.security.GeneralSecurityException;

import javax.security.auth.login.FailedLoginException;

import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.support.pac4j.authentication.principal.ClientCredential;
import org.jasig.cas.support.pac4j.test.MockFacebookClient;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.client.Clients;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.oauth.credentials.OAuthCredentials;
import org.pac4j.oauth.profile.facebook.FacebookProfile;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;

/**
 * Tests the {@link ClientAuthenticationHandler}.
 *
 * @author Jerome Leleu
 * @since 4.1.0
 *
 */
public final class ClientAuthenticationHandlerTests {

    private static final String CALLBACK_URL = "http://localhost:8080/callback";
    private static final String ID = "123456789";
    
    private MockFacebookClient fbClient;

    private ClientAuthenticationHandler handler;
    
    private ClientCredential clientCredential;

    @Before
    public void setUp() {
        this.fbClient = new MockFacebookClient();
        final Clients clients = new Clients(CALLBACK_URL, fbClient);
        this.handler = new ClientAuthenticationHandler(clients);
        final Credentials credentials = new OAuthCredentials(null, MockFacebookClient.CLIENT_NAME);
        this.clientCredential = new ClientCredential(credentials);
        ExternalContextHolder.setExternalContext(mock(ServletExternalContext.class));
    }
    
    @Test
    public void verifyOk() throws GeneralSecurityException, PreventedException {
        final FacebookProfile facebookProfile = new FacebookProfile();
        facebookProfile.setId(ID);
        this.fbClient.setFacebookProfile(facebookProfile);
        final HandlerResult result = this.handler.authenticate(this.clientCredential);
        final Principal principal = result.getPrincipal();
        assertEquals(FacebookProfile.class.getSimpleName() + "#" + ID, principal.getId());
    }

    @Test
    public void verifyOkWithSimpleIdentifier() throws GeneralSecurityException, PreventedException {
        this.handler.setTypedIdUsed(false);
        final FacebookProfile facebookProfile = new FacebookProfile();
        facebookProfile.setId(ID);
        this.fbClient.setFacebookProfile(facebookProfile);
        final HandlerResult result = this.handler.authenticate(this.clientCredential);
        final Principal principal = result.getPrincipal();
        assertEquals(ID, principal.getId());
    }

    @Test(expected = FailedLoginException.class)
    public void verifyNoProfile() throws GeneralSecurityException, PreventedException {
        this.handler.authenticate(this.clientCredential);
    }
}

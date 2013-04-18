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

import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.FailedLoginException;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.DirectMappingAuthenticationManagerImpl;
import org.jasig.cas.authentication.DirectMappingAuthenticationManagerImpl.DirectAuthenticationHandlerMappingHolder;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentialsToPrincipalResolver;
import org.junit.Before;
import org.junit.Test;


public class DirectMappingAuthenticationManagerImplTests {

    private DirectMappingAuthenticationManagerImpl manager = new DirectMappingAuthenticationManagerImpl();

    @Before
    public void setUp() throws Exception {
        this.manager = new DirectMappingAuthenticationManagerImpl();

        final Map<Class<? extends Credentials>, DirectAuthenticationHandlerMappingHolder> mappings = new HashMap<Class<? extends Credentials>, DirectAuthenticationHandlerMappingHolder>();
        final List<AuthenticationMetaDataPopulator> populators = new ArrayList<AuthenticationMetaDataPopulator>();
        populators.add(new SamlAuthenticationMetaDataPopulator());

        this.manager.setAuthenticationMetaDataPopulators(populators);

        final DirectAuthenticationHandlerMappingHolder d = new DirectAuthenticationHandlerMappingHolder();
        d.setAuthenticationHandler(new AuthenticationHandler() {
            @Override
            public HandlerResult authenticate(Credentials credential) throws GeneralSecurityException, PreventedException {
               final UsernamePasswordCredentials up = (UsernamePasswordCredentials) credential;
                if (up.getUsername().equals(up.getPassword())) {
                    return new HandlerResult(this);
                }
                throw new FailedLoginException();
            }

            @Override
            public boolean supports(Credentials credential) {
                return credential instanceof UsernamePasswordCredentials;
            }

            @Override
            public String getName() {
                return "Test";
            }
        });
        d.setCredentialsToPrincipalResolver(new UsernamePasswordCredentialsToPrincipalResolver());

        mappings.put(UsernamePasswordCredentials.class, d);

        this.manager.setCredentialsMapping(mappings);
    }

    @Test
    public void testAuthenticateUsernamePassword() throws Exception {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername("Test");
        c.setPassword("Test");
        final Authentication authentication = this.manager.authenticate(c);

        assertEquals(c.getUsername(), authentication.getPrincipal().getId());
    }

    @Test
    public void testAuthenticateBadUsernamePassword() throws Exception {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername("Test");
        c.setPassword("Test2");
        try {
            this.manager.authenticate(c);
            fail();
        } catch (final AuthenticationException e) {
            return;
        }
    }

    @Test
    public void testAuthenticateHttp() throws Exception {

        try {
            final HttpBasedServiceCredentials c = new HttpBasedServiceCredentials(new URL("http://www.cnn.com"));
            this.manager.authenticate(c);
            fail("Exception expected.");
        } catch (final AuthenticationException e) {
            assertEquals(0, e.getHandlerErrors().size());
            assertEquals(0, e.getHandlerSuccesses().size());
        }
    }
}

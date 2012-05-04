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
package org.jasig.cas.authentication;

import java.util.Arrays;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
import org.jasig.cas.authentication.handler.UnsupportedCredentialsException;
import org.jasig.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentialsToPrincipalResolver;
import org.jasig.cas.util.HttpClient;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class AuthenticationManagerImplTests extends AbstractCentralAuthenticationServiceTest {

    @Test
    public void testSuccessfulAuthentication() throws Exception {
        assertEquals(TestUtils.getPrincipal(),
            getAuthenticationManager().authenticate(
                TestUtils.getCredentialsWithSameUsernameAndPassword())
                .getPrincipal());
    }

    @Test
    public void testFailedAuthentication() throws Exception {
        try {
            getAuthenticationManager().authenticate(
                TestUtils.getCredentialsWithDifferentUsernameAndPassword());
            fail("Authentication should have failed.");
        } catch (AuthenticationException e) {
            return;
        }
    }

    @Test
    public void testNoHandlerFound() throws AuthenticationException {
        try {
            getAuthenticationManager().authenticate(new Credentials(){

                private static final long serialVersionUID = -4897240037527663222L;
                // there is nothing to do here
            });
            fail("Authentication should have failed.");
        } catch (UnsupportedCredentialsException e) {
            return;
        }
    }

    @Test(expected=UnsupportedCredentialsException.class)
    public void testNoResolverFound() throws Exception {
        AuthenticationManagerImpl manager = new AuthenticationManagerImpl();
        HttpBasedServiceCredentialsAuthenticationHandler authenticationHandler = new HttpBasedServiceCredentialsAuthenticationHandler();
        authenticationHandler.setHttpClient(new HttpClient());
        manager.setAuthenticationHandlers(Arrays.asList((AuthenticationHandler) authenticationHandler));
        manager.setCredentialsToPrincipalResolvers(Arrays.asList((CredentialsToPrincipalResolver) new UsernamePasswordCredentialsToPrincipalResolver()));
            manager.authenticate(TestUtils.getHttpBasedServiceCredentials());
    }

    @Test(expected = BadCredentialsAuthenticationException.class)
    public void testResolverReturnsNull() throws Exception {
        AuthenticationManagerImpl manager = new AuthenticationManagerImpl();
        HttpBasedServiceCredentialsAuthenticationHandler authenticationHandler = new HttpBasedServiceCredentialsAuthenticationHandler();
        authenticationHandler.setHttpClient(new HttpClient());
        manager
            .setAuthenticationHandlers(Arrays.asList((AuthenticationHandler) authenticationHandler));
        manager
            .setCredentialsToPrincipalResolvers(Arrays.asList((CredentialsToPrincipalResolver) new TestCredentialsToPrincipalResolver()));
            manager.authenticate(TestUtils.getHttpBasedServiceCredentials());
    }
    
    protected class TestCredentialsToPrincipalResolver implements CredentialsToPrincipalResolver {

        public Principal resolvePrincipal(Credentials credentials) {
            return null;
        }

        public boolean supports(final Credentials credentials) {
            return true;
        }
    }
}

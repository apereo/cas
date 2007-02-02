/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication;

import org.apache.commons.httpclient.HttpClient;
import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.handler.UnsupportedCredentialsException;
import org.jasig.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentialsToPrincipalResolver;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class AuthenticationManagerImplTests extends
    AbstractCentralAuthenticationServiceTest {

    public void testSuccessfulAuthentication() throws Exception {
        assertEquals(TestUtils.getPrincipal(),
            getAuthenticationManager().authenticate(
                TestUtils.getCredentialsWithSameUsernameAndPassword())
                .getPrincipal());
    }

    public void testFailedAuthentication() throws Exception {
        try {
            getAuthenticationManager().authenticate(
                TestUtils.getCredentialsWithDifferentUsernameAndPassword());
            fail("Authentication should have failed.");
        } catch (AuthenticationException e) {
            return;
        }
    }

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

    public void testNoResolverFound() throws Exception {
        AuthenticationManagerImpl manager = new AuthenticationManagerImpl();
        HttpBasedServiceCredentialsAuthenticationHandler authenticationHandler = new HttpBasedServiceCredentialsAuthenticationHandler();
        authenticationHandler.setHttpClient(new HttpClient());
        authenticationHandler.afterPropertiesSet();
        manager
            .setAuthenticationHandlers(new AuthenticationHandler[] {authenticationHandler});
        manager
            .setCredentialsToPrincipalResolvers(new CredentialsToPrincipalResolver[] {new UsernamePasswordCredentialsToPrincipalResolver()});
        manager.afterPropertiesSet();
        try {
            manager.authenticate(TestUtils.getHttpBasedServiceCredentials());
            fail("Authentication should have failed.");
        } catch (UnsupportedCredentialsException e) {
            return;
        }
    }
}

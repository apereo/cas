/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import java.net.URL;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.handler.UnsupportedCredentialsException;
import org.jasig.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentialsToPrincipalResolver;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class AuthenticationManagerImplTests extends AbstractCentralAuthenticationServiceTest {

    public void testSuccessfulAuthentication() throws Exception {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        Principal p = new SimplePrincipal("test");
        c.setUsername("test");
        c.setPassword("test");

        try {
            Authentication authentication = getAuthenticationManager().authenticate(c);
            assertEquals(p, authentication.getPrincipal());
        } catch (AuthenticationException e) {
            fail(e.getMessage());
        }
    }

    public void testFailedAuthentication() throws Exception {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUsername("test");
        c.setPassword("tt");

        try {
            getAuthenticationManager().authenticate(c);
            fail("Authentication should have failed.");
        } catch (AuthenticationException e) {
            return;
        }
    }

    public void testNoHandlerFound() throws Exception {
        try {
            getAuthenticationManager().authenticate(new Credentials() {

                /**
                 * Comment for <code>serialVersionUID</code>
                 */
                private static final long serialVersionUID = -4897240037527663222L;
                // there is nothing to do here
            });
            fail("Authentication should have failed.");
        } catch (UnsupportedCredentialsException e) {
            return;
        } catch (AuthenticationException e) {
            fail("UnsupportedCredentialsException expected.");
        }
    }

    public void testNoResolverFound() throws Exception {
        AuthenticationManagerImpl manager = new AuthenticationManagerImpl();
        manager.setAuthenticationHandlers(new AuthenticationHandler[] {new HttpBasedServiceCredentialsAuthenticationHandler()});
        manager.setCredentialsToPrincipalResolvers(new CredentialsToPrincipalResolver[] {new UsernamePasswordCredentialsToPrincipalResolver()});
        manager.afterPropertiesSet();
        try {
            manager.authenticate(new HttpBasedServiceCredentials(new URL("https://www.yale.edu")));
            fail("Authentication should have failed.");
        } catch (UnsupportedCredentialsException e) {
            return;
        } catch (AuthenticationException e) {
            fail("UnsupportedCredentialsException expected.");
        }
    }
}

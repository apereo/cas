/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import java.util.ArrayList;
import java.util.List;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.UnsupportedCredentialsException;
import org.jasig.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler;
import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.DefaultCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class AuthenticationManagerImplTests extends TestCase {
	AuthenticationManagerImpl manager;
	
	public void setUp() throws Exception {
		this.manager = new AuthenticationManagerImpl(); 
	}
	
	private void setUpManager(AuthenticationManagerImpl a) {
		List resolvers = new ArrayList();
		resolvers.add(new DefaultCredentialsToPrincipalResolver());
		resolvers.add(new HttpBasedServiceCredentialsToPrincipalResolver());
		a.setCredentialsToPrincipalResolvers(resolvers);
		
		List handlers = new ArrayList();
		handlers.add(new SimpleTestUsernamePasswordAuthenticationHandler());
		handlers.add(new HttpBasedServiceCredentialsAuthenticationHandler());
		
		a.setAuthenticationHandlers(handlers);
        
        List populators = new ArrayList();
        populators.add(new DefaultAuthenticationAttributesPopulator());
        a.setAuthenticationAttributesPopulators(populators);
	}
	
	private void setUpManager2(AuthenticationManagerImpl a) {
		List resolvers = new ArrayList();
		resolvers.add(new HttpBasedServiceCredentialsToPrincipalResolver());
		a.setCredentialsToPrincipalResolvers(resolvers);
		
		List handlers = new ArrayList();
		handlers.add(new SimpleTestUsernamePasswordAuthenticationHandler());
		handlers.add(new HttpBasedServiceCredentialsAuthenticationHandler());
		
		a.setAuthenticationHandlers(handlers);
	}
	
	public void testNoPropertiesSet() {
		try {
			this.manager.afterPropertiesSet();
			fail("Exception expected.");
		} catch (Exception e) {
			return;
		}
	}
	
	public void testProperties() {
		setUpManager(this.manager);
		try {
			this.manager.afterPropertiesSet();
		} catch (Exception e) {
			fail("Exception not expected.");
		}
	}
    
    public void testNoPopulators() {
        setUpManager2(this.manager);
        try {
            this.manager.afterPropertiesSet();
        } catch (Exception e) {
            fail("Exception not expected.");
        } 
    }
    
    public void testSuccessfulAuthentication() {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        Principal p = new SimplePrincipal("test");
        c.setUserName("test");
        c.setPassword("test");
        
        setUpManager(this.manager);
        try {
            Authentication authentication = this.manager.authenticate(c);
            assertEquals(p, authentication.getPrincipal());
        } catch (AuthenticationException e) {
            fail(e.getMessage());
        }
    }
    
    public void testFailedAuthentication() {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUserName("test");
        c.setPassword("tt");
        
        setUpManager(this.manager);
        try {
            this.manager.authenticate(c);
            fail("Authentication should have failed.");
        } catch (AuthenticationException e) {
            return;
        }
    }
	
	public void testNoHandlerFound() {
        setUpManager(this.manager);
        try {
            this.manager.authenticate(new TestCredentials());
            fail("Authentication should have failed.");
        } catch (UnsupportedCredentialsException e) {
            return;
        } catch (AuthenticationException e) {
			fail("UnsupportedCredentialsException expected.");
        }
	}
	
	public void testNoResolverFound() {
        setUpManager2(this.manager);
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setUserName("test");
        c.setPassword("test");
        try {
            this.manager.authenticate(c);
            fail("Authentication should have failed.");
        } catch (UnsupportedCredentialsException e) {
            return;
        } catch (AuthenticationException e) {
			fail("UnsupportedCredentialsException expected.");
        }
	}
	
	protected static class TestCredentials implements Credentials {

		private static final long serialVersionUID = 3258413949803246388L;
		
	}
}

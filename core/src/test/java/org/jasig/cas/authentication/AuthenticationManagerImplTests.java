/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import java.util.ArrayList;
import java.util.List;

import org.jasig.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler;
import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.DefaultCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentialsToPrincipalResolver;

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
			fail("Exception expected.");
		}
	}
}

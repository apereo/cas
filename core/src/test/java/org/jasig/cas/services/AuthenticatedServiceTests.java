/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.net.URL;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class AuthenticatedServiceTests extends TestCase {
	
	public void testGetters() {
		final String ID = "id";
		final boolean ALLOWTOPROXY = true;
		final boolean FORCEAUTHENTICATION = true;
		final String THEME = "theme";
		final SingleSignoutCallback callback = new SingleSignoutCallback() {public boolean sendSingleSignoutRequest(AuthenticatedService service, String test) {return false;} };
		final URL url = null;
		
		AuthenticatedService authenticatedService = new AuthenticatedService(ID, ALLOWTOPROXY, FORCEAUTHENTICATION, THEME, callback, url);
		
		assertEquals(ID, authenticatedService.getId());
		assertEquals(ALLOWTOPROXY, authenticatedService.isAllowedToProxy());
		assertEquals(FORCEAUTHENTICATION, authenticatedService.isForceAuthentication());
		assertEquals(THEME, authenticatedService.getTheme());
		assertEquals(callback, authenticatedService.getSingleSignoutCallback());
		assertEquals(url, authenticatedService.getProxyUrl());
	}
	
	public void testNoId() {
		try {
			new AuthenticatedService(null, false, false, null, null, null);
			fail("IllegalArgumentsException expected.");
		} catch (Exception e) {
			return;
		}
	}
}

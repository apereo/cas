/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.spnego.authentication.principal;

import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.support.spnego.authentication.principal.SpnegoCredentials;
import org.jasig.cas.support.spnego.authentication.principal.SpnegoCredentialsToPrincipalResolver;

import junit.framework.TestCase;

/**
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @version $Id$
 * @since 3.1
 * 
 */
public class SpnegoCredentialsToPrincipalResolverTests extends TestCase {
	private SpnegoCredentialsToPrincipalResolver resolver;

	private SpnegoCredentials spnegoCredentials;

	protected void setUp() throws Exception {
		this.resolver = new SpnegoCredentialsToPrincipalResolver();
		this.spnegoCredentials = new SpnegoCredentials(new byte[] { 0, 1, 2 });
	}

	public void testValidCredentials() {
		this.spnegoCredentials.setPrincipal(new SimplePrincipal("test"));
		assertEquals("test", this.resolver.resolvePrincipal(this.spnegoCredentials)
				.getId());
	}

	public void testSupports() {
		assertFalse(this.resolver.supports(null));
		assertTrue(this.resolver.supports(this.spnegoCredentials));
		assertFalse(this.resolver.supports(new UsernamePasswordCredentials()));
	}
}

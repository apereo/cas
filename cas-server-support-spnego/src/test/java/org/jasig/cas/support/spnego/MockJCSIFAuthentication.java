/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.spnego;

import java.security.Principal;

import jcifs.spnego.Authentication;
import jcifs.spnego.AuthenticationException;

/**
 * 
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @version $Id$
 * @since 3.1
 */
public class MockJCSIFAuthentication extends Authentication {
	private Principal principal;

	private boolean valid;

	private byte[] outToken = new byte[] { 4, 5, 6 };

	public MockJCSIFAuthentication(boolean valid) {
		this.principal = new MockPrincipal("test");
		this.valid = valid;

	}

	public byte[] getNextToken() {

		return this.valid ? this.outToken : null;
	}

	public java.security.Principal getPrincipal() {

		return this.valid ? this.principal : null;
	}

	public void process(byte[] arg0) throws AuthenticationException {
		if (!this.valid)
			throw new AuthenticationException("not valid");
	}

}
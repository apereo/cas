/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class UnsupportedCredentialsExceptionTests extends TestCase {

	public void testNoParamConstructor() {
		new UnsupportedCredentialsException();
	}
	
	public void testGetCode() {
		AuthenticationException e = new UnsupportedCredentialsException();
		assertEquals("error.authentication.credentials.unsupported", e.getCode());
	}
	
	
}

/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

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
	
	public void testMessageParamConstructor() {
		final String MESSAGE = "Test";
		AuthenticationException e = new UnsupportedCredentialsException(MESSAGE);
		assertEquals(MESSAGE, e.getMessage());
	}
	
	public void testMessageThrowableConstructor() {
		final String MESSAGE = "test";
		final Throwable THROWABLE = new Throwable();
		AuthenticationException e = new UnsupportedCredentialsException(MESSAGE,THROWABLE);
		
		assertEquals(MESSAGE, e.getMessage());
		assertEquals(THROWABLE, e.getCause());
	}
	
	public void testThrowableConstructor() {
		final Throwable THROWABLE = new Throwable();
		AuthenticationException e = new UnsupportedCredentialsException(THROWABLE);
		
		assertEquals(THROWABLE, e.getCause());
	}
	
	public void testGetCode() {
		AuthenticationException e = new UnsupportedCredentialsException();
		assertEquals("error.authentication.credentials.unsupported", e.getCode());
	}
	
	
}

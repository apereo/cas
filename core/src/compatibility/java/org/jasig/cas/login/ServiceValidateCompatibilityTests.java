/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */

package org.jasig.cas.login;

import java.io.IOException;

/**
 * Compatibility tests for /serviceValidate .
 * Note that this extends Cas2ValidateCompatibilityTests, which provides
 * several tests that apply to both service validate and proxy validate.
 * 
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class ServiceValidateCompatibilityTests 
	extends Cas2ValidateCompatibilityTests {

	/**
	 * @throws IOException
	 */
	public ServiceValidateCompatibilityTests() throws IOException {
		super();
	}
	
	/**
	 * @throws IOException
	 */
	public ServiceValidateCompatibilityTests(String name) throws IOException {
		super(name);
	}


	protected String getValidationPath() {
		return "/serviceValidate";
	}
	
	// TODO: add tests specific to /serviceValidate

}

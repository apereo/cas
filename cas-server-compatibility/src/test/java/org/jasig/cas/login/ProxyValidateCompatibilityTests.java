/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */

package org.jasig.cas.login;

import java.io.IOException;

/**
 * Compatibility tests for /proxyValidate .
 * Note that this extends Cas2ValidateCompatibilityTests, which provides
 * several tests that apply to both service validate and proxy validate.
 * 
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class ProxyValidateCompatibilityTests 
	extends AbstractCas2ValidateCompatibilityTests {

	/**
	 * @throws IOException
	 */
	public ProxyValidateCompatibilityTests() throws IOException {
		super();
	}
	
	/**
	 * @throws IOException
	 */
	public ProxyValidateCompatibilityTests(String name) throws IOException {
		super(name);
	}


	protected String getValidationPath() {
		return "/proxyValidate";
	}
	
	// TODO: add tests specific to /proxyValidate

}

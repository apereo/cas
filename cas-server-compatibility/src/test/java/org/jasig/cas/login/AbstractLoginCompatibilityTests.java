/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.login;

import java.io.IOException;


public abstract class AbstractLoginCompatibilityTests extends AbstractCompatibilityTests {
    public static final String FORM_USERNAME = "username";
    public static final String FORM_PASSWORD = "password";
    
    /**
     * The name of the compatibility test configuration property the value of which
     * should be an alternate username to test logging into the CAS server.
     */
    public static final String ALTERNATE_USERNAME_PROPERTY = "credentials.alternate.username";
    
    /**
     * The name of the compatibility test configuration property the value of which should be 
     * the correct password to go with that alternate username.
     */
    public static final String ALTERNATE_PASSWORD_PROPERTY = "credentials.alternate.password";

    public AbstractLoginCompatibilityTests() throws IOException {
        super();
    }

    public AbstractLoginCompatibilityTests(String name) throws IOException {
        super(name);
    }
    
    protected String getAlternateUsername(){
    	return getProperties().getProperty(ALTERNATE_USERNAME_PROPERTY);
    }
    
    protected String getAlternatePassword() {
    	return getProperties().getProperty(ALTERNATE_PASSWORD_PROPERTY);
    }
	

    
}

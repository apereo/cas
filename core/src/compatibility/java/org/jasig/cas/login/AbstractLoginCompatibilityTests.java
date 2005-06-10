/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.login;

import java.io.IOException;


public abstract class AbstractLoginCompatibilityTests extends AbstractCompatibilityTests {
    public static final String FORM_USERNAME = "username";
    public static final String FORM_PASSWORD = "password";
    
    /**
     * The name of the compatibility test configuration property the value of
     * which will be the username as whom we should try to authenticate.
     */
    public static final String USERNAME_PROPERTY = "credentials.username";
    
    /**
     * The name of the compatibility test configuration property the value of
     * which will be a correct password for the username.
     */
    public static final String GOOD_PASSWORD_PROPERTY = "credentials.goodPassword";
    
    /**
     * The name of the compatibility test configuration property the value of 
     * which will be an incorrect password for the username.
     */
    public static final String BAD_PASSWORD_PROPERTY = "credentials.badPassword";

    public AbstractLoginCompatibilityTests() throws IOException {
        super();
    }

    public AbstractLoginCompatibilityTests(String name) throws IOException {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        beginAt("/login");
    }
    
    /**
     * Get the username as which we should test authenticating.
     * @return the username
     */
    protected String getUsername(){
    	return getProperties().getProperty(USERNAME_PROPERTY);
    }
    
    /**
     * Get the correct password for authenticating as the username given by
     * getUsername().
     * @return the correct password
     */
    protected String getGoodPassword() {
    	return getProperties().getProperty(GOOD_PASSWORD_PROPERTY);
    }
    
    /**
     * Get an incorrect password for the username given by getUsername().
     * @return an incorrect password.
     */
    protected String getBadPassword() {
    	return getProperties().getProperty(BAD_PASSWORD_PROPERTY);
    }
}

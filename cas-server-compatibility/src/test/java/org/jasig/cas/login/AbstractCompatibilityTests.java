/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.login;

import java.io.IOException;
import java.util.Properties;

import org.springframework.core.io.ClassPathResource;

import net.sourceforge.jwebunit.WebTestCase;


public abstract class AbstractCompatibilityTests extends WebTestCase {

    public static final String LOGIN_TOKEN = "lt";
    
    public static final String COOKIE_TGC_ID = "CASTGC";
    
	/**
	 * The name of our properties configuration while, which we expect on the
	 * classpath.
	 */
	public static final String PROPERTIES_FILE_NAME = "configuration.properties";
	
    final private Properties properties = new Properties();
    
    /**
     * The name of the compatibility test configuration property the value of which
     * will be the base URL of the CAS server, e.g. for Yale's production CAS server
     * server.url=https://secure.its.yale.edu/cas
     */
    public static final String SERVER_URL_PROPERTY = "server.url";
    
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
    
    
    /**
     * The name of the compatibility test configuration property the value of
     * which will be a URL to a service we can use for testing.
     */
    public static final String SERVICE_URL_PROPERTY = "service.url";
    
    protected AbstractCompatibilityTests() throws IOException {
        super();
        setUpTest();
    }
    
    protected AbstractCompatibilityTests(final String name) throws IOException {
        super(name);
        setUpTest();
    }
    
    private final void setUpTest() throws IOException {
        this.properties.load(new ClassPathResource(PROPERTIES_FILE_NAME).getInputStream());
        getTestContext().setBaseUrl(this.properties.getProperty(SERVER_URL_PROPERTY));
    }
    
    /**
     * Get the Properties parsed at instantiation from the compatibility
     * tests configuration file.
     * @return Properties from our configuration file.
     */
    protected final Properties getProperties() {
    	return this.properties;
    }
    
    /**
     * Get the username as which we should test authenticating.
     * @return the username
     */
    protected final String getUsername(){
    	return getProperties().getProperty(USERNAME_PROPERTY);
    }
    
    /**
     * Get the correct password for authenticating as the username given by
     * getUsername().
     * @return the correct password
     */
    protected final String getGoodPassword() {
    	return getProperties().getProperty(GOOD_PASSWORD_PROPERTY);
    }
    
    /**
     * Get an incorrect password for the username given by getUsername().
     * @return an incorrect password.
     */
    protected final String getBadPassword() {
    	return getProperties().getProperty(BAD_PASSWORD_PROPERTY);
    }
    
    /**
     * Get the configured URL we are to use as an example service for which
     * we acquire service tickets.
     * @return example service URL
     */
    protected final String getServiceUrl() {
    	return getProperties().getProperty(SERVICE_URL_PROPERTY);
    }
}

/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.login;

import java.io.IOException;
import java.util.Properties;

import net.sourceforge.jwebunit.WebTestCase;


public abstract class AbstractCompatibilityTests extends WebTestCase {

	/**
	 * The name of our properties configuration while, which we expect on the
	 * classpath.
	 */
	public static final String PROPERTIES_FILE_NAME = "configuration.properties";
	
    final private Properties properties = new Properties();
    
    protected AbstractCompatibilityTests() throws IOException {
        super();
        setUpTest();
    }
    
    protected AbstractCompatibilityTests(final String name) throws IOException {
        super(name);
        setUpTest();
    }
    
    private final void setUpTest() throws IOException {
        this.properties.load(ClassLoader.getSystemResourceAsStream(PROPERTIES_FILE_NAME));
        getTestContext().setBaseUrl(this.properties.getProperty("server.url"));
    }
    
    /**
     * Get the Properties parsed at instantiation from the compatibility
     * tests configuration file.
     * @return Properties from our configuration file.
     */
    protected final Properties getProperties() {
    	return this.properties;
    }
}

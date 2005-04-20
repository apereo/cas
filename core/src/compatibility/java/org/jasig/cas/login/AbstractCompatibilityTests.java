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
        this.properties.load(ClassLoader.getSystemResourceAsStream("urls.properties"));
        getTestContext().setBaseUrl(this.properties.getProperty("server.url"));
    }
}

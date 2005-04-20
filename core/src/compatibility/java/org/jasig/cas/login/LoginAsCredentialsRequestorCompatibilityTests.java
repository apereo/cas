/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.login;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;

import net.sourceforge.jwebunit.WebTestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class LoginAsCredentialsRequestorCompatibilityTests extends WebTestCase {
    final Properties properties = new Properties();
    
    public LoginAsCredentialsRequestorCompatibilityTests() throws IOException {
        super();
        setUpTest();
    }
    
    public LoginAsCredentialsRequestorCompatibilityTests(final String name) throws IOException {
        super(name);
        setUpTest();
    }
    
    private final void setUpTest() throws IOException {
        this.properties.load(ClassLoader.getSystemResourceAsStream("urls.properties"));
        getTestContext().setBaseUrl(this.properties.getProperty("server.url"));
    }

    protected void setUp() throws Exception {
        super.setUp();
        beginAt("/login");
    }

    public void testGatewayWithServiceWithNoTgt() throws UnsupportedEncodingException {
        final String GATEWAY = "yes";
        final String SERVICE = URLEncoder.encode("http://www.cnn.com", "UTF-8");
        final String URL = "/login?service=" + SERVICE + "&gateway=" + GATEWAY;
         
        beginAt(URL);
        assertTextPresent("cnn.com");
    }
    
}

/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.login;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.jasig.cas.web.support.WebConstants;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class LoginAsCredentialsRequestorCompatibilityTests extends AbstractLoginCompatibilityTests {
    
    
    public LoginAsCredentialsRequestorCompatibilityTests() throws IOException {
        super();
    }
    
    public LoginAsCredentialsRequestorCompatibilityTests(final String name) throws IOException {
        super(name);
    }

    public void testGatewayWithServiceWithNoTgt() throws UnsupportedEncodingException {
        final String GATEWAY = "yes";
        final String SERVICE = URLEncoder.encode("http://www.cnn.com", "UTF-8");
        final String URL = "/login?service=" + SERVICE + "&gateway=" + GATEWAY;
         
        beginAt(URL);
        assertTextPresent("cnn.com");
    }
    
    public void testServiceWithSingleSignOn() {
        setFormElement(FORM_USERNAME, "test");
        setFormElement(FORM_PASSWORD, "test");
        submit();
        // TODO test single sign on
    }
    
    public void testGatewayWithNoService() {
        final String GATEWAY = "yes";
        final String URL = "/login?gateway=" + GATEWAY;
        
        beginAt(URL);
        assertFormElementPresent(WebConstants.LOGIN_TOKEN);
    }
    
    public void testGatewayWithServiceWithTgt() {
        
        //TODO: complete the test for a Gateway request with a Service
    }
    
    public void testExistingTgtRenewEqualsTrue() {
        //TODO: complete the test for a renew=true and existing TGT
    }
    
    public void testTrustHandling() {
        // TODO test trust handling
    }
    
    public void testInitialFormParameters() {
        assertFormElementPresent(FORM_USERNAME);
        assertFormElementPresent(FORM_PASSWORD);
        assertFormElementPresent(WebConstants.LOGIN_TOKEN);
    }
}

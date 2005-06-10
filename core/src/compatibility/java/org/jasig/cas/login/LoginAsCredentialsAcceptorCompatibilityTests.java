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
 * @author Drew Mazurek
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class LoginAsCredentialsAcceptorCompatibilityTests extends AbstractLoginCompatibilityTests {

    public LoginAsCredentialsAcceptorCompatibilityTests() throws IOException {
        super();
    }

    public LoginAsCredentialsAcceptorCompatibilityTests(String name) throws IOException {
        super(name);
    }
    
    public void testSingleSignOn() {
        setFormElement(FORM_USERNAME, getUsername());
        setFormElement(FORM_PASSWORD, getGoodPassword());
        submit();
        assertCookiePresent(WebConstants.COOKIE_TGC_ID);
        assertFormNotPresent();
    }
    
    public void testValidCredentialsAuthenticationWithWarn() throws IOException {
        final String service = "http://www.yale.edu";
        beginAt("/login?service=" + URLEncoder.encode(service, "UTF-8"));
        setFormElement(FORM_USERNAME, getUsername());
        setFormElement(FORM_PASSWORD, getGoodPassword());
        getDialog().getForm().setCheckbox("warn", true);
        submit();
        
        final String anotherService = "https://secure.its.yale.edu/cas";
        final String anotherServiceEncoded = URLEncoder.encode(anotherService, "UTF-8");
        
        beginAt("/login?service=" + anotherServiceEncoded);
        
        // since warn was set, CAS should not redirect us immediately to the service,
        // but should rather interpose a warning screen.
        
        assertTextPresent(anotherService);
        
        
    }

    public void testValidCredentialsAuthenticationWithoutWarn() throws UnsupportedEncodingException {
    	final String service = "http://www.cnn.com";
        beginAt("/login?service=" + URLEncoder.encode(service, "UTF-8"));
        setFormElement(FORM_USERNAME, getUsername());
        setFormElement(FORM_PASSWORD, getGoodPassword());
        submit();
        // TODO testValidCredentialsAuthenticationWithoutWarn
    }
    
    /*
     * jWebUnit doesn't allow you to change pre-populated hidden form values.
     * 
    public void testBadLoginTicket() {
        setFormElement(FORM_USERNAME, "test");
        setFormElement(FORM_PASSWORD, "test");
        setFormElement(WebConstants.LOGIN_TOKEN, "test");

        submit();
        assertFormElementPresent(FORM_USERNAME);
    }
    
    public void testNoLoginTicket() {
        setFormElement(FORM_USERNAME, "test");
        setFormElement(FORM_PASSWORD, "test");
        setFormElement(WebConstants.LOGIN_TOKEN, "");
        submit();
        assertFormElementPresent(FORM_USERNAME);
    }
        
    public void testDoubleLoginTicket() {
        //TODO: covered by badLoginTicket?
    }
     *
     */
    
    public void testPassBadCredentials() {
        setFormElement(FORM_USERNAME, getUsername());
        setFormElement(FORM_PASSWORD, getBadPassword());
        submit();
        assertFormElementPresent(FORM_USERNAME);
    }
    
    public void testPassEmptyCredentials() {
        submit();
        assertFormElementPresent(FORM_USERNAME);
    }

}

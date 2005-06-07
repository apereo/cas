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
        setFormElement(FORM_USERNAME, "test");
        setFormElement(FORM_PASSWORD, "test");
        submit();
        assertCookiePresent(WebConstants.COOKIE_TGC_ID);
        assertFormNotPresent();
    }
    
    public void testValidCredentialsAuthenticationWithWarn() throws UnsupportedEncodingException {
        final String service = "https://localhost:8443/contacts-cas/j_acegi_cas_security_check";
        beginAt("/login?service=" + URLEncoder.encode(service, "UTF-8"));
        setFormElement(FORM_USERNAME, "test");
        setFormElement(FORM_PASSWORD, "test");
        getDialog().getForm().setCheckbox("warn", true);
        submit();
//        checkCheckbox("warn");
        assertTextPresent(service);
        assertCookiePresent(WebConstants.COOKIE_PRIVACY);
        assertCookiePresent(WebConstants.COOKIE_TGC_ID);
    }

    public void testValidCredentialsAuthenticationWithoutWarn() throws UnsupportedEncodingException {
    	final String service = "http://www.cnn.com";
        beginAt("/login?service=" + URLEncoder.encode(service, "UTF-8"));
        setFormElement(FORM_USERNAME, "test");
        setFormElement(FORM_PASSWORD, "test");
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
        setFormElement(FORM_USERNAME, "test");
        setFormElement(FORM_PASSWORD, "duh");
        submit();
        assertFormElementPresent(FORM_USERNAME);
    }
    
    public void testPassEmptyCredentials() {
        submit();
        assertFormElementPresent(FORM_USERNAME);
    }

}

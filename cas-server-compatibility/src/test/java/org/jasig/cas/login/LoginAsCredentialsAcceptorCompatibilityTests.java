/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.login;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
    	beginAt("/login");
        setFormElement(FORM_USERNAME, getUsername());
        setFormElement(FORM_PASSWORD, getGoodPassword());
        submit();
        assertCookiePresent(COOKIE_TGC_ID);
        assertFormNotPresent();
        
        // TODO test logging in to another service
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
    	beginAt("/login");
        setFormElement(FORM_USERNAME, getUsername());
        setFormElement(FORM_PASSWORD, getBadPassword());
        submit();
        assertFormElementPresent(FORM_USERNAME);
    }
    
    public void testPassEmptyCredentials() {
    	beginAt("/login");
        submit();
        assertFormElementPresent(FORM_USERNAME);
    }
    
    /**
     * Test that logging in as someone else destroys the TGT and outstanding 
     * service tickets for the previously authenticated user.
     * @throws IOException
     */
    public void testLoginAsSomeoneElse() throws IOException {
    	String encodedService = URLEncoder.encode(getServiceUrl(), "UTF-8");
    	
    	// establish SSO session as the first user
    	
    	beginAt("/login?service=" + encodedService);
        setFormElement(FORM_USERNAME, getUsername());
        setFormElement(FORM_PASSWORD, getGoodPassword());
        submit();
        
        // get the service ticket
        
        String firstServiceTicket = LoginHelper.serviceTicketFromResponse(getDialog().getResponse());
        
        // now login via renew as someone else
        
        
        beginAt("/login?renew=true&service=" + encodedService);
        setFormElement(FORM_USERNAME, getAlternateUsername());
        setFormElement(FORM_PASSWORD, getAlternatePassword());
        submit();
        
        // get the service ticket
        
        String secondServiceTicket = LoginHelper.serviceTicketFromResponse(getDialog().getResponse());
        
        // validate the second service ticket
        
        beginAt("/serviceValidate?ticket=" + secondServiceTicket + "&service=" + encodedService);
        
        assertTextPresent("<cas:user>" + getAlternateUsername() + "</cas:user>");
        
        
        // okay, now attempt to validate the original service ticket
        // and see that it has been invalidated
        
        beginAt("/serviceValidate?ticket=" + firstServiceTicket + "&service=" + encodedService);
        
        System.out.println(getDialog().getResponseText());
        assertTextPresent("<cas:authenticationFailure");
        
    }

}

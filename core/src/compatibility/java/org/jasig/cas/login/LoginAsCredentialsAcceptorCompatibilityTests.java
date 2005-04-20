/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.login;

import java.io.IOException;

/**
 * 
 * @author Scott Battaglia
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
    
    public void testValidCredentialsAuthenticationWithWarn() {
        
    }

    public void testValidCredentialsAuthenticationWithoutWarn() {
        
    }
    
    public void testNoLoginTicket() {
        setFormElement("username", "test");
        setFormElement("password", "test");
        setFormElement("lt", "");
        submit();
        assertFormElementPresent("username");
    }
    
    public void testBadLoginTicket() {
        setFormElement("username", "test");
        setFormElement("password", "test");
        setFormElement("lt", "test");
        submit();
        assertFormElementPresent("username");
    }
    
    public void testDoubleLoginTicket() {
        //TODO: covered by badLoginTicket?
    }
    
    public void testPassBadCredentials() {
        setFormElement("username", "test");
        setFormElement("password", "duh");
        submit();
        assertFormElementPresent("username");
    }
    
    public void testPassEmptyCredentials() {
        submit();
        assertFormElementPresent("username");
    }

}

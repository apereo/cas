/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import junit.framework.TestCase;
import org.jasig.cas.adaptors.cas.LegacyCasTrustedCredentials;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;


/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class FileAuthenticationHandlerTest extends TestCase {

    private AuthenticationHandler authenticationHandler;
    
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
	    super.setUp();
	    this.authenticationHandler = new FileAuthenticationHandler();
        ((FileAuthenticationHandler) this.authenticationHandler).setFileName("authentication.txt");

	}
    public void testSupportsProperUserCredentials() {
        assertTrue(this.authenticationHandler.supports(new UsernamePasswordCredentials()));
    }
    
    public void testDoesntSupportBadUserCredentials() {
        assertFalse(this.authenticationHandler.supports(new LegacyCasTrustedCredentials()));
    }
    
    public void testAuthenticatesUserInFileWithDefaultSeparator() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        
        c.setUserName("scott");
        c.setPassword("rutgers");
        
        try {
        	assertTrue(this.authenticationHandler.authenticate(c));
        } catch (AuthenticationException e) {
            fail("AuthenticationException caught but it should not have been thrown.");
        }
    }
    
    public void testFailsUserNotInFileWithDefaultSeparator() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        
        c.setUserName("fds");
        c.setPassword("rutgers");
        
        try {
        assertFalse(this.authenticationHandler.authenticate(c));
        } catch (AuthenticationException e) {
            // this is okay because it means the test failed.
        }
    }
    
    public void testFailsNullUserName() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        
        c.setUserName(null);
        c.setPassword("user");
        
        try {
        assertFalse(this.authenticationHandler.authenticate(c));
        } catch (AuthenticationException e) {
            // this is okay because it means the test failed.
        }
    }
    
    public void testFailsNullUserNameAndPassword() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        
        c.setUserName(null);
        c.setPassword(null);
        
        try {
        assertFalse(this.authenticationHandler.authenticate(c));
        } catch (AuthenticationException e) {
            // this is okay because it means the test failed.
        }
    }
    
    public void testFailsNullPassword() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        
        c.setUserName("scott");
        c.setPassword(null);
        
        try {
        assertFalse(this.authenticationHandler.authenticate(c));
        } catch (AuthenticationException e) {
            // this is okay because it means the test failed.
        }
    }
    
    public void testAuthenticatesUserInFileWithCommaSeparator() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        
        ((FileAuthenticationHandler) this.authenticationHandler).setFileName("authentication2.txt");
        ((FileAuthenticationHandler) this.authenticationHandler).setSeparator(",");
        
        c.setUserName("scott");
        c.setPassword("rutgers");
        
        try {
        	assertTrue(this.authenticationHandler.authenticate(c));
        } catch (AuthenticationException e) {
            fail("AuthenticationException caught but it should not have been thrown.");
        }
    }
    
    public void testFailsUserNotInFileWithCommaSeparator() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        
        ((FileAuthenticationHandler) this.authenticationHandler).setFileName("authentication2.txt");
        ((FileAuthenticationHandler) this.authenticationHandler).setSeparator(",");
        
        c.setUserName("fds");
        c.setPassword("rutgers");
        
        try {
        assertFalse(this.authenticationHandler.authenticate(c));
        } catch (AuthenticationException e) {
            // this is okay because it means the test failed.
        }
    }
}

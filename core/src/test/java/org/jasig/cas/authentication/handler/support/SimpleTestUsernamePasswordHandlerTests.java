/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import java.net.MalformedURLException;
import java.net.URL;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.UnsupportedCredentialsException;
import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import junit.framework.TestCase;

/**
 * Test of the simple username/password handler
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class SimpleTestUsernamePasswordHandlerTests extends TestCase {

    private SimpleTestUsernamePasswordAuthenticationHandler authenticationHandler = new SimpleTestUsernamePasswordAuthenticationHandler();

    public void testSupportsProperUserCredentials() {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUserName("test");
        c.setPassword("test");
        try {
            this.authenticationHandler.authenticate(c);
        }
        catch (UnsupportedCredentialsException e) {
            fail("UnsupportedCredentialsException caught");
        }
        catch (AuthenticationException e) {
            fail("AuthenticationException caught.");
        }
    }

    public void testDoesntSupportBadUserCredentials() {
        try {
            this.authenticationHandler.authenticate(new HttpBasedServiceCredentials(new URL("http://www.rutgers.edu")));
        }
        catch (MalformedURLException e) {
            fail("Could not resolve URL.");
        }
        catch (UnsupportedCredentialsException e) {
            // this is okay
        }
        catch (AuthenticationException e) {
            fail("AuthenticationException caught.");
        }
    }

    public void testValidUsernamePassword() {
        UsernamePasswordCredentials authRequest = new UsernamePasswordCredentials();
        authRequest.setUserName("test");
        authRequest.setPassword("test");

        try {
            assertTrue(this.authenticationHandler.authenticate(authRequest));
        }
        catch (AuthenticationException ae) {
            fail();
        }
    }

    public void testInvalidUsernamePassword() {
        UsernamePasswordCredentials authRequest = new UsernamePasswordCredentials();
        authRequest.setUserName("test");
        authRequest.setPassword("test2");

        try {
            assertFalse(this.authenticationHandler.authenticate(authRequest));
        }
        catch (AuthenticationException ae) {
            // this is okay
        }
    }

    public void testNullUsernamePassword() {
        UsernamePasswordCredentials authRequest = new UsernamePasswordCredentials();
        authRequest.setUserName(null);
        authRequest.setPassword(null);

        try {
            assertFalse(this.authenticationHandler.authenticate(authRequest));
        }
        catch (AuthenticationException ae) {
            // this is okay
        }
    }
    
    public void testAfterPropertiesSet() {
        try {
            this.authenticationHandler.afterPropertiesSet();
        } catch (Exception e) {
            fail("Exception caught but none should have been thrown.");
        }
    }
}
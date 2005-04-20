/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import java.net.MalformedURLException;
import java.net.URL;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.UnsupportedCredentialsException;
import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

import junit.framework.TestCase;

/**
 * Test of the simple username/password handler
 * 
 * @author Scott Battaglia
 * @version $Id: SimpleTestUsernamePasswordHandlerTests.java,v 1.3 2005/02/27
 * 05:49:26 sbattaglia Exp $
 */
public class SimpleTestUsernamePasswordHandlerTests extends TestCase {

    private SimpleTestUsernamePasswordAuthenticationHandler authenticationHandler = new SimpleTestUsernamePasswordAuthenticationHandler();

    public void testSupportsProperUserCredentials() {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUsername("test");
        c.setPassword("test");
        try {
            this.authenticationHandler.authenticate(c);
        } catch (UnsupportedCredentialsException e) {
            fail("UnsupportedCredentialsException caught");
        } catch (AuthenticationException e) {
            fail("AuthenticationException caught.");
        }
    }

    public void testDoesntSupportBadUserCredentials() {
        try {
            assertFalse(this.authenticationHandler
                .supports(new HttpBasedServiceCredentials(new URL(
                    "http://www.rutgers.edu"))));
        } catch (MalformedURLException e) {
            fail("Could not resolve URL.");
        }
    }

    public void testValidUsernamePassword() {
        UsernamePasswordCredentials authRequest = new UsernamePasswordCredentials();
        authRequest.setUsername("test");
        authRequest.setPassword("test");

        try {
            assertTrue(this.authenticationHandler.authenticate(authRequest));
        } catch (AuthenticationException ae) {
            fail();
        }
    }

    public void testInvalidUsernamePassword() {
        UsernamePasswordCredentials authRequest = new UsernamePasswordCredentials();
        authRequest.setUsername("test");
        authRequest.setPassword("test2");

        try {
            assertFalse(this.authenticationHandler.authenticate(authRequest));
        } catch (AuthenticationException ae) {
            // this is okay
        }
    }

    public void testNullUsernamePassword() {
        UsernamePasswordCredentials authRequest = new UsernamePasswordCredentials();
        authRequest.setUsername(null);
        authRequest.setPassword(null);

        try {
            assertFalse(this.authenticationHandler.authenticate(authRequest));
        } catch (AuthenticationException ae) {
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
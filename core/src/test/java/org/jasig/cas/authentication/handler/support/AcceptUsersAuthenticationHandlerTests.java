/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.UnsupportedCredentialsException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public class AcceptUsersAuthenticationHandlerTests extends TestCase {

    final private Map users;

    final private AuthenticationHandler authenticationHandler;

    public AcceptUsersAuthenticationHandlerTests() {
        this.users = new HashMap();

        this.users.put("scott", "rutgers");
        this.users.put("dima", "javarules");
        this.users.put("bill", "thisisAwesoME");

        this.authenticationHandler = new AcceptUsersAuthenticationHandler();

        ((AcceptUsersAuthenticationHandler)this.authenticationHandler).setUsers(this.users);
    }

    public void testSupportsProperUserCredentials() {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUserName("scott");
        c.setPassword("rutgers");
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

    public void testAuthenticatesUserInMap() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUserName("scott");
        c.setPassword("rutgers");

        try {
            assertTrue(this.authenticationHandler.authenticate(c));
        }
        catch (AuthenticationException e) {
            fail("AuthenticationException caught but it should not have been thrown.");
        }
    }

    public void testFailsUserNotInMap() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUserName("fds");
        c.setPassword("rutgers");

        try {
            assertFalse(this.authenticationHandler.authenticate(c));
        }
        catch (AuthenticationException e) {
            // this is okay because it means the test failed.
        }
    }

    public void testFailsNullUserName() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUserName(null);
        c.setPassword("user");

        try {
            assertFalse(this.authenticationHandler.authenticate(c));
        }
        catch (AuthenticationException e) {
            // this is okay because it means the test failed.
        }
    }

    public void testFailsNullUserNameAndPassword() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUserName(null);
        c.setPassword(null);

        try {
            assertFalse(this.authenticationHandler.authenticate(c));
        }
        catch (AuthenticationException e) {
            // this is okay because it means the test failed.
        }
    }

    public void testFailsNullPassword() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUserName("scott");
        c.setPassword(null);

        try {
            assertFalse(this.authenticationHandler.authenticate(c));
        }
        catch (AuthenticationException e) {
            // this is okay because it means the test failed.
        }
    }
}
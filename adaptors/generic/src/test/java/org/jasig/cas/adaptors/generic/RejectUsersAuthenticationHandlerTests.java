/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.generic;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.jasig.cas.adaptors.generic.RejectUsersAuthenticationHandler;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.UnsupportedCredentialsException;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Id: RejectUsersAuthenticationHandlerTests.java,v 1.3 2005/02/27
 * 05:49:26 sbattaglia Exp $
 */
public class RejectUsersAuthenticationHandlerTests extends TestCase {

    final private Collection users;

    final private RejectUsersAuthenticationHandler authenticationHandler;

    public RejectUsersAuthenticationHandlerTests() {
        this.users = new ArrayList();

        this.users.add("scott");
        this.users.add("dima");
        this.users.add("bill");

        this.authenticationHandler = new RejectUsersAuthenticationHandler();

        this.authenticationHandler.setUsers(this.users);
    }

    public void testSupportsProperUserCredentials() {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUserName("fff");
        c.setPassword("rutgers");
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
            this.authenticationHandler
                .authenticate(new HttpBasedServiceCredentials(new URL(
                    "http://www.rutgers.edu")));
        } catch (MalformedURLException e) {
            fail("Could not resolve URL.");
        } catch (UnsupportedCredentialsException e) {
            // this is okay
        } catch (AuthenticationException e) {
            fail("AuthenticationException caught.");
        }
    }

    public void testFailsUserInMap() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUserName("scott");
        c.setPassword("rutgers");

        try {
            assertFalse(this.authenticationHandler.authenticate(c));
        } catch (AuthenticationException e) {
            // fail("AuthenticationException caught but it should not have been
            // thrown.");
        }
    }

    public void testPassesUserNotInMap() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();

        c.setUserName("fds");
        c.setPassword("rutgers");

        try {
            assertTrue(this.authenticationHandler.authenticate(c));
        } catch (AuthenticationException e) {
            fail("Exception thrown but not expected.");
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

    public void testAfterPropertiesSetWithNullUsers() {
        try {
            this.authenticationHandler.setUsers(null);
            this.authenticationHandler.afterPropertiesSet();
            fail("Exception expected.");
        } catch (Exception e) {
            return;
        }
    }

    public void testAfterPropertiesSetWithNonNullUsers() {
        try {
            this.authenticationHandler.afterPropertiesSet();

        } catch (Exception e) {
            fail("Exception not expected.");
        }
    }
}
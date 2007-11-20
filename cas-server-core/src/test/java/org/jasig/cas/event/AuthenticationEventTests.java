/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.event;

import java.util.Date;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.handler.AuthenticationHandler;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class AuthenticationEventTests extends TestCase {

    public void testGetCredentials() {
        assertEquals("Credentials are not equal.", TestUtils
            .getCredentialsWithSameUsernameAndPassword().toString(),
            new AuthenticationEvent(TestUtils
                .getCredentialsWithSameUsernameAndPassword().toString(), true,
                AuthenticationHandler.class).getPrincipal());
    }

    public void testIsSuccessful() {
        assertTrue("Authentication success is false.", new AuthenticationEvent(
            TestUtils.getCredentialsWithSameUsernameAndPassword().toString(), true,
            AuthenticationHandler.class).isSuccessfulAuthentication());
    }

    public void testIsNotSuccessful() {
        assertFalse("Authentication success is true.", new AuthenticationEvent(
            TestUtils.getCredentialsWithSameUsernameAndPassword().toString(), false,
            AuthenticationHandler.class).isSuccessfulAuthentication());
    }

    public void testPublishedDate() {
        final long startDate = System.currentTimeMillis();
        final Date publishedDate = new AuthenticationEvent(
            TestUtils.getCredentialsWithSameUsernameAndPassword().toString(), false,
            AuthenticationHandler.class).getPublishedDate();
        final long endDate = System.currentTimeMillis();
        
        assertTrue(startDate <= publishedDate.getTime());
        assertTrue(endDate >= publishedDate.getTime());
    }

    public void testAuthenticationClass() {
        assertEquals("AuthenticationHandler classes not equal.",
            AuthenticationHandler.class, new AuthenticationEvent(TestUtils
                .getCredentialsWithSameUsernameAndPassword().toString(), false,
                AuthenticationHandler.class).getAuthenticationHandlerClass());
    }
}

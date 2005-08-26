/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.event;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
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
            .getCredentialsWithSameUsernameAndPassword(),
            new AuthenticationEvent(TestUtils
                .getCredentialsWithSameUsernameAndPassword(), true,
                AuthenticationHandler.class).getCredentials());
    }

    public void testIsSuccessful() {
        assertTrue("Authentication success is false.", new AuthenticationEvent(
            TestUtils.getCredentialsWithSameUsernameAndPassword(), true,
            AuthenticationHandler.class).isSuccessfulAuthentication());
    }

    public void testIsNotSuccessful() {
        assertFalse("Authentication success is true.", new AuthenticationEvent(
            TestUtils.getCredentialsWithSameUsernameAndPassword(), false,
            AuthenticationHandler.class).isSuccessfulAuthentication());
    }

    public void testPublishedDate() {
        assertEquals("Dates not equal.", new Date(), new AuthenticationEvent(
            TestUtils.getCredentialsWithSameUsernameAndPassword(), false,
            AuthenticationHandler.class).getPublishedDate());
    }

    public void testAuthenticationClass() {
        assertEquals("AuthenticationHandler classes not equal.",
            AuthenticationHandler.class, new AuthenticationEvent(TestUtils
                .getCredentialsWithSameUsernameAndPassword(), false,
                AuthenticationHandler.class).getAuthenticationHandlerClass());
    }

    public void testToString() {
        final AuthenticationEvent authenticationEvent = new AuthenticationEvent(
            TestUtils.getCredentialsWithSameUsernameAndPassword(), true,
            AuthenticationHandler.class);
        assertEquals("ToStrings not equal.", ToStringBuilder
            .reflectionToString(authenticationEvent), authenticationEvent
            .toString());
    }
}

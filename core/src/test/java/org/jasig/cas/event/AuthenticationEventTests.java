/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
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

    private AuthenticationEvent authenticationEvent;

    public void testGetCredentials() {
        this.authenticationEvent = new AuthenticationEvent(TestUtils
            .getCredentialsWithSameUsernameAndPassword(), true,
            AuthenticationHandler.class);

        assertEquals(TestUtils.getCredentialsWithSameUsernameAndPassword(),
            this.authenticationEvent.getCredentials());
    }

    public void testIsSuccessful() {
        this.authenticationEvent = new AuthenticationEvent(TestUtils
            .getCredentialsWithSameUsernameAndPassword(), true,
            AuthenticationHandler.class);
        assertTrue(this.authenticationEvent.isSuccessfulAuthentication());
    }

    public void testIsNotSuccessful() {
        this.authenticationEvent = new AuthenticationEvent(TestUtils
            .getCredentialsWithSameUsernameAndPassword(), false,
            AuthenticationHandler.class);
        assertFalse(this.authenticationEvent.isSuccessfulAuthentication());
    }

    public void testPublishedDate() {
        this.authenticationEvent = new AuthenticationEvent(TestUtils
            .getCredentialsWithSameUsernameAndPassword(), false,
            AuthenticationHandler.class);
        assertEquals(new Date(), this.authenticationEvent.getPublishedDate());
    }

    public void testAuthenticationClass() {
        this.authenticationEvent = new AuthenticationEvent(TestUtils
            .getCredentialsWithSameUsernameAndPassword(), false,
            AuthenticationHandler.class);
        assertEquals(AuthenticationHandler.class, this.authenticationEvent
            .getAuthenticationHandlerClass());
    }

    public void testToString() {
        this.authenticationEvent = new AuthenticationEvent(TestUtils
            .getCredentialsWithSameUsernameAndPassword(), true,
            AuthenticationHandler.class);
        assertEquals(ToStringBuilder
            .reflectionToString(this.authenticationEvent),
            this.authenticationEvent.toString());
    }
}

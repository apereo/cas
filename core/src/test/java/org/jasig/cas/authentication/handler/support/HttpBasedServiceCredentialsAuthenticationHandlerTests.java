/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class HttpBasedServiceCredentialsAuthenticationHandlerTests extends
    TestCase {

    final private AuthenticationHandler authenticationHandler;

    public HttpBasedServiceCredentialsAuthenticationHandlerTests() {
        this.authenticationHandler = new HttpBasedServiceCredentialsAuthenticationHandler();
    }

    public void testSupportsProperUserCredentials() {
        assertTrue(this.authenticationHandler.supports(TestUtils
            .getHttpBasedServiceCredentials()));
    }

    public void testDoesntSupportBadUserCredentials() {
        assertFalse(this.authenticationHandler.supports(TestUtils
            .getCredentialsWithSameUsernameAndPassword()));
    }

    public void testAcceptsProperCertificateCredentials() {
        try {
            assertTrue(this.authenticationHandler.authenticate(TestUtils
                .getHttpBasedServiceCredentials()));
        } catch (AuthenticationException e) {
            fail("We should not have gotten an error.");
        }
    }

    public void testRejectsInProperCertificateCredentials() {
        try {
            assertFalse(this.authenticationHandler
                .authenticate(TestUtils
                    .getHttpBasedServiceCredentials("https://clue.acs.rutgers.edu")));
        } catch (AuthenticationException e) {
            // this is okay;
        }
    }

    public void testRejectsNonHttpsCredentials() {
        try {
            assertFalse(this.authenticationHandler.authenticate(TestUtils
                .getHttpBasedServiceCredentials("http://www.jasig.org")));
        } catch (AuthenticationException e) {
            // this is okay.
        }
    }

    public void testAllowNullResponse() {
        try {
            ((HttpBasedServiceCredentialsAuthenticationHandler) this.authenticationHandler)
                .setAllowNullResponses(true);
            assertTrue(this.authenticationHandler.authenticate(TestUtils
                .getHttpBasedServiceCredentials()));
        } catch (AuthenticationException e) {
            fail("We should not have gotten an error.");
        }
    }
}
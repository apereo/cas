/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */

package org.jasig.cas.adaptors.cas;

import javax.servlet.ServletRequest;

import org.jasig.cas.adaptors.cas.mock.MockPasswordHandler;
import org.springframework.mock.web.MockHttpServletRequest;

import junit.framework.TestCase;

/**
 * Testcase for LegacyPasswordHandlerAdaptorAuthenticationHandler.
 * 
 * @version $Revision$ $Date$
 */
public class LegacyPasswordHandlerAdaptorAuthenticationHandlerTests extends
    TestCase {

    private LegacyPasswordHandlerAdaptorAuthenticationHandler lphaah;

    protected void setUp() throws Exception {
        super.setUp();
        this.lphaah = new LegacyPasswordHandlerAdaptorAuthenticationHandler();
        this.lphaah.setPasswordHandler(new MockPasswordHandler());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSupports() {
        assertFalse(this.lphaah.supports(null));
        assertTrue(this.lphaah.supports(new LegacyCasCredentials()));
        assertFalse(this.lphaah.supports(new LegacyCasTrustedCredentials()));
    }

    /**
     * Test that throws UnsupportedCredentialsException for a known unsupported
     * credential.
     * 
     * @throws AuthenticationException as a failure modality
     */
    public void testAuthenticateUnsupported() {
        this.lphaah.supports(new LegacyCasTrustedCredentials());
    }

    public void testAuthenticateSuccess() {
        // configure the PasswordHandler.
        MockPasswordHandler mockHandler = new MockPasswordHandler();
        mockHandler.setSucceed(true);
        this.lphaah.setPasswordHandler(mockHandler);

        // configure the LegacyCasCredentials
        LegacyCasCredentials credentials = new LegacyCasCredentials();
        credentials.setUsername("testUser");
        credentials.setPassword("testPassword");
        ServletRequest servletRequest = new MockHttpServletRequest();
        credentials.setServletRequest(servletRequest);

        assertTrue(this.lphaah.authenticate(credentials));

        assertEquals("testUser", mockHandler.getUsername());
        assertEquals("testPassword", mockHandler.getPassword());
        assertSame(servletRequest, mockHandler.getRequest());

    }

    public void testAuthenticateFailure() {
        // configure the PasswordHandler.
        MockPasswordHandler mockHandler = new MockPasswordHandler();
        mockHandler.setSucceed(false);
        this.lphaah.setPasswordHandler(mockHandler);

        // configure the LegacyCasCredentials
        LegacyCasCredentials credentials = new LegacyCasCredentials();
        credentials.setUsername("testUser");
        credentials.setPassword("testPassword");
        ServletRequest servletRequest = new MockHttpServletRequest();
        credentials.setServletRequest(servletRequest);

        assertFalse(this.lphaah.authenticate(credentials));

        assertEquals("testUser", mockHandler.getUsername());
        assertEquals("testPassword", mockHandler.getPassword());
        assertSame(servletRequest, mockHandler.getRequest());

    }

}

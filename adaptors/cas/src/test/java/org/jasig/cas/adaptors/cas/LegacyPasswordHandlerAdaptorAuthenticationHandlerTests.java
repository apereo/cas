/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */

package org.jasig.cas.adaptors.cas;

import javax.servlet.ServletRequest;

import org.jasig.cas.adaptors.cas.mock.MockPasswordHandler;
import org.jasig.cas.adaptors.cas.mock.MockServletRequest;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.UnsupportedCredentialsException;

import junit.framework.TestCase;

/**
 * Testcase for LegacyPasswordHandlerAdaptorAuthenticationHandler.
 * @version $Revision$ $Date$
 */
public class LegacyPasswordHandlerAdaptorAuthenticationHandlerTests extends
        TestCase {

    private LegacyPasswordHandlerAdaptorAuthenticationHandler lphaah;

    protected void setUp() throws Exception {
        super.setUp();
        this.lphaah = 
            new LegacyPasswordHandlerAdaptorAuthenticationHandler();
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
     * Document LPHAAH's NPE behavior where its dependency has not been set.
     * Mitigated by use of afterPropertiesSet() to sanity check.  
     * Consider using constructor dependency injection to guarantee that
     * dependecy has been set to non-null value.
     * @throws AuthenticationException as a failure modality
     */
    public void testAuthenticateMissingHandler() throws AuthenticationException {
        try {
            this.lphaah.authenticate(new LegacyCasCredentials());
        } catch (NullPointerException npe) {
            // throws NPE when dependency is not satisified.
            // TODO: Reconsider other approaches to avoid this NPE.
            return;
        }
        fail("Behavior we were trying to document was an NPE that wasn't thrown?");
    }
    
    /**
     * Test that throws UnsupportedCredentialsException for a known unsupported
     * credential.
     * @throws AuthenticationException as a failure modality
     */
    public void testAuthenticateUnsupported() throws AuthenticationException {
        try {
            this.lphaah.authenticate(new LegacyCasTrustedCredentials());
        } catch (UnsupportedCredentialsException uce) {
            // good
            return;
        }
        fail("Expected UnssupportedCredentialsException.");
    }

    public void testAuthenticateSuccess() throws AuthenticationException {
        // configure the PasswordHandler.
        MockPasswordHandler mockHandler = new MockPasswordHandler();
        mockHandler.setSucceed(true);
        this.lphaah.setPasswordHandler(mockHandler);
        
        // configure the LegacyCasCredentials
        LegacyCasCredentials credentials = new LegacyCasCredentials();
        credentials.setUserName("testUser");
        credentials.setPassword("testPassword");
        ServletRequest servletRequest = new MockServletRequest();
        credentials.setServletRequest(servletRequest);
        
        assertTrue(this.lphaah.authenticate(credentials));
        
        assertEquals("testUser", mockHandler.getUsername());
        assertEquals("testPassword", mockHandler.getPassword());
        assertSame(servletRequest, mockHandler.getRequest());
        
    }
    
    public void testAuthenticateFailure() throws AuthenticationException {
        // configure the PasswordHandler.
        MockPasswordHandler mockHandler = new MockPasswordHandler();
        mockHandler.setSucceed(false);
        this.lphaah.setPasswordHandler(mockHandler);
        
        // configure the LegacyCasCredentials
        LegacyCasCredentials credentials = new LegacyCasCredentials();
        credentials.setUserName("testUser");
        credentials.setPassword("testPassword");
        ServletRequest servletRequest = new MockServletRequest();
        credentials.setServletRequest(servletRequest);
        
        assertFalse(this.lphaah.authenticate(credentials));
        
        assertEquals("testUser", mockHandler.getUsername());
        assertEquals("testPassword", mockHandler.getPassword());
        assertSame(servletRequest, mockHandler.getRequest());
        
    }
    
}


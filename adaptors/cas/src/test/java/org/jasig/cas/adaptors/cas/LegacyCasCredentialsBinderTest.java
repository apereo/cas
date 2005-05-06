/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.cas;

import org.jasig.cas.authentication.principal.Credentials;
import org.springframework.mock.web.MockHttpServletRequest;

import junit.framework.TestCase;

/**
 * Partial testcase for LegacyCasCredentialsBinder.
 * @version $Revision$ $Date$
 */
public class LegacyCasCredentialsBinderTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test that binding succeeds for a subclass of LegacyCasCredentials..
     */
    public void testBindCasCredentialsSubclass() {
        LegacyCasCredentialsBinder binder = new LegacyCasCredentialsBinder();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        LegacyCasCredentialsSubclass legacyCasCredentials 
            = new LegacyCasCredentialsSubclass();
        binder.bind(mockRequest, legacyCasCredentials);
        
        assertSame(mockRequest, legacyCasCredentials.getServletRequest());
    }
    
    /**
     * Test that binding succeeds for a subclass of LegacyCasTrustedCredentials.
     */
    public void testBindTrustedCasCredentialsSubclass() {
        LegacyCasCredentialsBinder binder = new LegacyCasCredentialsBinder();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        LegacyCasTrustedCredentialsSubclass legacyCasTrustedCredentials 
            = new LegacyCasTrustedCredentialsSubclass();
        
        binder.bind(mockRequest, legacyCasTrustedCredentials);
        
        assertSame(mockRequest, legacyCasTrustedCredentials.getServletRequest());
    }
    
    /**
     * Tests that we support LegacyCasCredentials and LegacyCasTrustedCredentials 
     * and that we do not support some adhoc Credentials that are not and
     * do not subclass these credentials.
     */
    public void testSupports() {
        LegacyCasCredentialsBinder binder = new LegacyCasCredentialsBinder();
        assertTrue(binder.supports(LegacyCasCredentials.class));
        assertTrue(binder.supports(LegacyCasTrustedCredentials.class));
        assertFalse(binder.supports(AdHocUnsupportedCredentials.class));
    }

    /**
     * Test that binding succeeds for a subclass of LegacyCasCredentials
     * and for a subclass of LegacyCasTrustedCredentials.
     */
    public void testSupportsSubclasses() {
        
        LegacyCasCredentialsBinder binder = new LegacyCasCredentialsBinder();
        assertTrue(binder.supports(LegacyCasCredentialsSubclass.class));
        assertTrue(binder.supports(LegacyCasTrustedCredentialsSubclass.class));
        
    }

    /**
     * A dummy subclass of LegacyCasCredentials that we use to test that 
     * LegacyCasCredentialsBinder binds subclasses of LegacyCasCredentials.
     */
    private class LegacyCasCredentialsSubclass extends LegacyCasCredentials {
        // just being a subclass is sufficient for our testing needs.
    }
    
    /**
     * A dummy subclass of LegacyCasTrustedCredentials that we use to test that 
     * LegacyCasCredentialsBinder binds subclasses of LegacyCasTrustedCredentials.
     */
    private class LegacyCasTrustedCredentialsSubclass extends LegacyCasTrustedCredentials {
        // just being a subclass is sufficient for our testing needs.
    }
    
    /**
     * We test that we do not support these adhoc non-legacy do-nothing credentials.
     */
    private class AdHocUnsupportedCredentials implements Credentials {
        // does nothing
    }
}


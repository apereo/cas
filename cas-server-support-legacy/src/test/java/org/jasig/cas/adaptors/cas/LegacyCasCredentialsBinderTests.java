/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.cas;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.web.bind.CredentialsBinder;
import org.springframework.mock.web.MockHttpServletRequest;

import junit.framework.TestCase;

/**
 * Partial test case for LegacyCasCredentialsBinder.
 * 
 * @version $Revision$ $Date$
 */
public class LegacyCasCredentialsBinderTests extends TestCase {

    private CredentialsBinder credentialsBinder = new LegacyCasCredentialsBinder();

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Tests that we support LegacyCasCredentials and
     * LegacyCasTrustedCredentials and that we do not support some adhoc
     * Credentials that are not and do not subclass these credentials.
     */
    public void testSupports() {
        assertTrue(this.credentialsBinder.supports(LegacyCasCredentials.class));
        assertTrue(this.credentialsBinder.supports(LegacyCasTrustedCredentials.class));
        assertFalse(this.credentialsBinder.supports(AdHocUnsupportedCredentials.class));
    }

    public void testBindMethod() {
        HttpServletRequest request = new MockHttpServletRequest();
        LegacyCasCredentials credentials = new LegacyCasCredentials();

        this.credentialsBinder.bind(request, credentials);

        assertEquals(request, credentials.getServletRequest());
    }
    
    public void testBindMethodWithTrust() {
        HttpServletRequest request = new MockHttpServletRequest();
        LegacyCasTrustedCredentials credentials = new LegacyCasTrustedCredentials();

        this.credentialsBinder.bind(request, credentials);

        assertEquals(request, credentials.getServletRequest());
    }

    /**
     * We test that we do not support these adhoc non-legacy do-nothing
     * credentials.
     */
    private class AdHocUnsupportedCredentials implements Credentials {

        private static final long serialVersionUID = 3257285812100936752L;
        // does nothing
    }
}

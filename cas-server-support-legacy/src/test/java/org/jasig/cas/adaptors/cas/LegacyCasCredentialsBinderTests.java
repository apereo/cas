/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

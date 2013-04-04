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

import static org.junit.Assert.*;

import javax.servlet.ServletRequest;

import org.jasig.cas.adaptors.cas.mock.MockTrustHandler;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Testcase for LegacyTrustAdaptorAuthenticationHandler.
 */
public class LegacyTrustHandlerAdaptorAuthenticationHandlerTests {

    private LegacyTrustHandlerAdaptorAuthenticationHandler legacyTrustAdaptor;

    @Before
    public void setUp() throws Exception {
        this.legacyTrustAdaptor = new LegacyTrustHandlerAdaptorAuthenticationHandler();
        this.legacyTrustAdaptor.setTrustHandler(new MockTrustHandler());
    }

    /**
     * Currently, tests that the adaptor does not support null credentials,
     * supports an instance of LegacyCasTrustedCredentials, and does not support
     * an instance of LegacyCasCredentials.
     */
    @Test
    public void testSupports() {
        assertFalse(this.legacyTrustAdaptor.supports(null));
        LegacyCasTrustedCredentials goodCred = new LegacyCasTrustedCredentials();
        assertTrue(this.legacyTrustAdaptor.supports(goodCred));
        LegacyCasCredentials badCred = new LegacyCasCredentials();
        assertFalse(this.legacyTrustAdaptor.supports(badCred));
    }

    /**
     * Test a successful authentication.
     * @throws AuthenticationException as one failure modality
     */
    @Test
    public void testAuthenticate() {
        LegacyCasTrustedCredentials trustedCredentials = new LegacyCasTrustedCredentials();

        ServletRequest request = new MockHttpServletRequest();

        trustedCredentials.setServletRequest(request);

        MockTrustHandler mockTrustHandler = new MockTrustHandler();

        mockTrustHandler.setUserName("testUser");

        this.legacyTrustAdaptor.setTrustHandler(mockTrustHandler);

        assertTrue(this.legacyTrustAdaptor.authenticate(trustedCredentials));

        assertSame(request, mockTrustHandler.getRequest());

    }

    /**
     * Test an unsuccessful authentication.
     * @throws AuthenticationException - as one failure modality
     */
    @Test
    public void testAuthenticateFails() {
        LegacyCasTrustedCredentials trustedCredentials = new LegacyCasTrustedCredentials();

        ServletRequest request = new MockHttpServletRequest();

        trustedCredentials.setServletRequest(request);

        MockTrustHandler mockTrustHandler = new MockTrustHandler();

        mockTrustHandler.setUserName(null);

        this.legacyTrustAdaptor.setTrustHandler(mockTrustHandler);

        assertFalse(this.legacyTrustAdaptor.authenticate(trustedCredentials));

        assertSame(request, mockTrustHandler.getRequest());

    }

    /**
     * Test that throws UnsupportedCredentialsException for an unsupported
     * credential.
     * @throws AuthenticationException
     */
    @Test
    public void testAuthenticateUnsupported() {
        LegacyCasCredentials badCredentials = new LegacyCasCredentials();
        assertFalse(this.legacyTrustAdaptor.supports(badCredentials));
    }
}

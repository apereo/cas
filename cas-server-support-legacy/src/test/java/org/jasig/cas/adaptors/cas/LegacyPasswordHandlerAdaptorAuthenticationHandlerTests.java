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

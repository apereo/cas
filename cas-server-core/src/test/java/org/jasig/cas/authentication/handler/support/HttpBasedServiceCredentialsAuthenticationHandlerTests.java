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
package org.jasig.cas.authentication.handler.support;

import javax.security.auth.login.FailedLoginException;

import org.jasig.cas.TestUtils;
import org.jasig.cas.util.SimpleHttpClient;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0
 */
public final class HttpBasedServiceCredentialsAuthenticationHandlerTests {

    private HttpBasedServiceCredentialsAuthenticationHandler authenticationHandler;

    @Before
    public void setUp() throws Exception {
        this.authenticationHandler = new HttpBasedServiceCredentialsAuthenticationHandler();
        this.authenticationHandler.setHttpClient(new SimpleHttpClient());
    }

    @Test
    public void testSupportsProperUserCredentials() {
        assertTrue(this.authenticationHandler.supports(TestUtils.getHttpBasedServiceCredentials()));
    }

    @Test
    public void testDoesntSupportBadUserCredentials() {
        assertFalse(this.authenticationHandler.supports(TestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void testAcceptsProperCertificateCredentials() throws Exception {
        assertNotNull(this.authenticationHandler.authenticate(TestUtils.getHttpBasedServiceCredentials()));
    }

    @Test(expected = FailedLoginException.class)
    public void testRejectsInProperCertificateCredentials() throws Exception {
        this.authenticationHandler.authenticate(
                TestUtils.getHttpBasedServiceCredentials("https://clearinghouse.ja-sig.org"));
    }

    @Test(expected = FailedLoginException.class)
    public void testRejectsNonHttpsCredentials() throws Exception {
        this.authenticationHandler.authenticate(TestUtils.getHttpBasedServiceCredentials("http://www.jasig.org"));
    }

    @Test
    public void testAcceptsNonHttpsCredentials() throws Exception {
        this.authenticationHandler.setHttpClient(new SimpleHttpClient());
        this.authenticationHandler.setRequireSecure(false);
        assertNotNull(this.authenticationHandler.authenticate(
                TestUtils.getHttpBasedServiceCredentials("http://www.jasig.org")));
    }

    @Test(expected = FailedLoginException.class)
    public void testNoAcceptableStatusCode() throws Exception {
        this.authenticationHandler.authenticate(
                TestUtils.getHttpBasedServiceCredentials("https://clue.acs.rutgers.edu"));
    }

    @Test(expected = FailedLoginException.class)
    public void testNoAcceptableStatusCodeButOneSet() throws Exception {
        final SimpleHttpClient httpClient = new SimpleHttpClient();
        httpClient.setAcceptableCodes(new int[] {900});
        this.authenticationHandler.setHttpClient(httpClient);
        this.authenticationHandler.authenticate(TestUtils.getHttpBasedServiceCredentials("https://www.ja-sig.org"));
    }
}

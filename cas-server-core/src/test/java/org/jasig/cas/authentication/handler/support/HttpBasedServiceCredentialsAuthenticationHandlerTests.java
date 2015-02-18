/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

import org.jasig.cas.TestUtils;
import org.jasig.cas.util.http.HttpClient;
import org.jasig.cas.util.http.SimpleHttpClientFactoryBean;
import org.junit.Before;
import org.junit.Test;

import javax.security.auth.login.FailedLoginException;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public final class HttpBasedServiceCredentialsAuthenticationHandlerTests {

    private HttpBasedServiceCredentialsAuthenticationHandler authenticationHandler;

    @Before
    public void setUp() throws Exception {
        this.authenticationHandler = new HttpBasedServiceCredentialsAuthenticationHandler();
        this.authenticationHandler.setHttpClient(new SimpleHttpClientFactoryBean().getObject());
    }

    @Test
    public void verifySupportsProperUserCredentials() {
        assertTrue(this.authenticationHandler.supports(TestUtils.getHttpBasedServiceCredentials()));
    }

    @Test
    public void verifyDoesntSupportBadUserCredentials() {
        assertFalse(this.authenticationHandler.supports(TestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifyAcceptsProperCertificateCredentials() throws Exception {
        assertNotNull(this.authenticationHandler.authenticate(TestUtils.getHttpBasedServiceCredentials()));
    }

    @Test(expected = FailedLoginException.class)
    public void verifyRejectsInProperCertificateCredentials() throws Exception {
        this.authenticationHandler.authenticate(
                TestUtils.getHttpBasedServiceCredentials("https://clearinghouse.ja-sig.org"));
    }

    @Test
    public void verifyAcceptsNonHttpsCredentials() throws Exception {
        this.authenticationHandler.setHttpClient(new SimpleHttpClientFactoryBean().getObject());
        assertNotNull(this.authenticationHandler.authenticate(
                TestUtils.getHttpBasedServiceCredentials("http://www.google.com")));
    }

    @Test(expected = FailedLoginException.class)
    public void verifyNoAcceptableStatusCode() throws Exception {
        this.authenticationHandler.authenticate(
                TestUtils.getHttpBasedServiceCredentials("https://clue.acs.rutgers.edu"));
    }

    @Test(expected = FailedLoginException.class)
    public void verifyNoAcceptableStatusCodeButOneSet() throws Exception {
        final SimpleHttpClientFactoryBean clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setAcceptableCodes(new int[] {900});
        final HttpClient httpClient = clientFactory.getObject();
        this.authenticationHandler.setHttpClient(httpClient);
        this.authenticationHandler.authenticate(TestUtils.getHttpBasedServiceCredentials("https://www.ja-sig.org"));
    }
}

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
package org.jasig.cas.authentication;

import java.security.GeneralSecurityException;

import junit.framework.TestCase;
import org.jasig.cas.TestUtils;
import org.jasig.cas.util.HttpClient;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class HttpBasedServiceCredentialsAuthenticationHandlerTests extends
    TestCase {

    private HttpBasedServiceCredentialsAuthenticationHandler authenticationHandler;

    protected void setUp() throws Exception {
        this.authenticationHandler = new HttpBasedServiceCredentialsAuthenticationHandler();
        this.authenticationHandler.setHttpClient(new HttpClient());
    }

    public void testSupportsProperUserCredentials() {
        assertTrue(this.authenticationHandler.supports(TestUtils
            .getHttpBasedServiceCredentials()));
    }

    public void testDoesntSupportBadUserCredentials() {
        assertFalse(this.authenticationHandler.supports(TestUtils
            .getCredentialsWithSameUsernameAndPassword()));
    }

    public void testAcceptsProperCertificateCredentials() throws Exception {
        final HandlerResult result = this.authenticationHandler.authenticate(
                TestUtils.getHttpBasedServiceCredentials());
        assertEquals(this.authenticationHandler.getName(), result.getHandlerName());

    }

    public void testRejectsInProperCertificateCredentials() throws Exception {
        try {
            this.authenticationHandler.authenticate(
                    TestUtils.getHttpBasedServiceCredentials("https://clearinghouse.ja-sig.org"));
            fail("Authentication succeeded when it should have failed.");
        } catch (GeneralSecurityException e) {}
    }

    public void testRejectsNonHttpsCredentials() throws Exception {
         try {
            this.authenticationHandler.authenticate(
                    TestUtils.getHttpBasedServiceCredentials("http://www.jasig.org"));
            fail("Authentication succeeded when it should have failed.");
        } catch (GeneralSecurityException e) {}
    }
    
    public void testAcceptsNonHttpsCredentials() throws Exception {
        this.authenticationHandler.setHttpClient(new HttpClient());
        this.authenticationHandler.setRequireSecure(false);
        final HandlerResult result = this.authenticationHandler.authenticate(
                TestUtils.getHttpBasedServiceCredentials("http://www.jasig.org"));
        assertEquals(this.authenticationHandler.getName(), result.getHandlerName());
    }

    public void testNoAcceptableStatusCode() throws Exception {
        try {
            this.authenticationHandler.authenticate(
                    TestUtils.getHttpBasedServiceCredentials("https://clue.acs.rutgers.edu"));
            fail("Authentication succeeded when it should have failed.");
        } catch (GeneralSecurityException e) {}
    }
    
    public void testNoAcceptableStatusCodeButOneSet() throws Exception {
        final HttpClient httpClient = new HttpClient();
        httpClient.setAcceptableCodes(new int[] {900});
        this.authenticationHandler.setHttpClient(httpClient);
        try {
            this.authenticationHandler.authenticate(
                    TestUtils.getHttpBasedServiceCredentials("https://www.ja-sig.org"));
            fail("Authentication succeeded when it should have failed.");
        } catch (GeneralSecurityException e) {}
    }
}
/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication.handler.support;

import org.jasig.cas.TestUtils;
import org.jasig.cas.util.HttpClient;

import junit.framework.TestCase;

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

    public void testAcceptsProperCertificateCredentials() {
        assertTrue(this.authenticationHandler.authenticate(TestUtils
            .getHttpBasedServiceCredentials()));
    }

    public void testRejectsInProperCertificateCredentials() {
        assertFalse(this.authenticationHandler.authenticate(TestUtils
            .getHttpBasedServiceCredentials("https://clearinghouse.ja-sig.org")));
    }

    public void testRejectsNonHttpsCredentials() {
        assertFalse(this.authenticationHandler.authenticate(TestUtils
            .getHttpBasedServiceCredentials("http://www.jasig.org")));
    }
    
    public void testAcceptsNonHttpsCredentials() {
        this.authenticationHandler.setHttpClient(new HttpClient());
        this.authenticationHandler.setRequireSecure(false);
        assertTrue(this.authenticationHandler.authenticate(TestUtils
            .getHttpBasedServiceCredentials("http://www.jasig.org")));
    }

    public void testNoAcceptableStatusCode() throws Exception {
        assertFalse(this.authenticationHandler.authenticate(TestUtils
            .getHttpBasedServiceCredentials("https://clue.acs.rutgers.edu")));
    }
    
    public void testNoAcceptableStatusCodeButOneSet() throws Exception {
        final HttpClient httpClient = new HttpClient();
        httpClient.setAcceptableCodes(new int[] {900});
        this.authenticationHandler.setHttpClient(httpClient);
        assertFalse(this.authenticationHandler.authenticate(TestUtils
            .getHttpBasedServiceCredentials("https://www.ja-sig.org")));
    }
}
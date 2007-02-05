/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import org.apache.commons.httpclient.HttpClient;
import org.jasig.cas.TestUtils;
import org.jasig.cas.util.HttpClient3FactoryBean;

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
        HttpClient3FactoryBean factory = new HttpClient3FactoryBean();
        factory.setUseStrictHostNameChecking(true);
        factory.afterPropertiesSet();
        this.authenticationHandler.setHttpClient((HttpClient) factory.getObject());
        this.authenticationHandler.afterPropertiesSet();
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
            .getHttpBasedServiceCredentials("http://www.ja-sig.org")));
    }

    public void testNoAcceptableStatusCode() throws Exception {
        this.authenticationHandler.setAcceptableCodes(new int[0]);
        this.authenticationHandler.afterPropertiesSet();
        assertFalse(this.authenticationHandler.authenticate(TestUtils
            .getHttpBasedServiceCredentials("https://clue.acs.rutgers.edu")));
    }
    
    public void testNoAcceptableStatusCodeButOneSet() throws Exception {
        this.authenticationHandler.setAcceptableCodes(new int[] {567});
        this.authenticationHandler.afterPropertiesSet();
        assertFalse(this.authenticationHandler.authenticate(TestUtils
            .getHttpBasedServiceCredentials("https://www.ja-sig.org")));
    }
}
/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import java.net.MalformedURLException;
import java.net.URL;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public class HttpBasedServiceCredentialsAuthenticationHandlerTest extends TestCase {

	final private URL httpBackedUrl;

	final private URL httpsProperCertificateUrl;

	final private URL httpsInproperCertificateUrl;

	final private AuthenticationHandler authenticationHandler;

	public HttpBasedServiceCredentialsAuthenticationHandlerTest() throws MalformedURLException {
		this.httpBackedUrl = new URL("http://www.ja-sig.org");
		this.httpsProperCertificateUrl = new URL("https://www.acs.rutgers.edu");
		this.httpsInproperCertificateUrl = new URL("https://clue.acs.rutgers.edu/");
		this.authenticationHandler = new HttpBasedServiceCredentialsAuthenticationHandler();
	}

	public void testSupportsProperUserCredentials() {
        try {
            final HttpBasedServiceCredentials c = new HttpBasedServiceCredentials(this.httpsInproperCertificateUrl);
        	this.authenticationHandler.authenticate(c);
        } catch (AuthenticationException e) {
            fail("AuthenticationException caught.");
        }
	}

	public void testDoesntSupportBadUserCredentials() {
        try {
        	this.authenticationHandler.authenticate(new UsernamePasswordCredentials());
        } catch (AuthenticationException e) {
        	return;
        }
        fail("AuthenticationException expected.");
	}

	public void testAcceptsProperCertificateCredentials() {
		try {
			assertTrue(this.authenticationHandler.authenticate(new HttpBasedServiceCredentials(this.httpsProperCertificateUrl)));
		} catch (AuthenticationException e) {
			fail("We should not have gotten an error.");
		}
	}

	public void testRejectsInProperCertificateCredentials() {
		try {
			assertFalse(this.authenticationHandler.authenticate(new HttpBasedServiceCredentials(this.httpsInproperCertificateUrl)));
		} catch (AuthenticationException e) {
			// this is okay;
		}
	}

	public void testRejectsNonHttpsCredentials() {
		try {
			assertFalse(this.authenticationHandler.authenticate(new HttpBasedServiceCredentials(this.httpBackedUrl)));
		} catch (AuthenticationException e) {
			// this is okay.
		}
	}
}

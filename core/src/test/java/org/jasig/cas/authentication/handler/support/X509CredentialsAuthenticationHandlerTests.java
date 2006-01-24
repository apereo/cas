/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import java.security.cert.X509Certificate;

import org.jasig.cas.AbstractX509CertificateTests;
import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.X509CertificateCredentials;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class X509CredentialsAuthenticationHandlerTests extends AbstractX509CertificateTests {
    private AuthenticationHandler authenticationHandler;

    protected void setUp() throws Exception {
        final X509CredentialsAuthenticationHandler ah = new X509CredentialsAuthenticationHandler();
        ah.setTrustedIssuers(new String[] {"rutgers", "JA-SIG"});
        ah.afterPropertiesSet();
        
        this.authenticationHandler = ah;
    }
    
    public void testSupportsClass() {
        assertTrue(this.authenticationHandler.supports(new X509CertificateCredentials(new X509Certificate[0])));
    }
    
    public void testDoesntSupportClass() {
        assertFalse(this.authenticationHandler.supports(TestUtils.getCredentialsWithSameUsernameAndPassword()));
    }
    
    public void testInvalidCertificate() throws Exception {
        final X509CertificateCredentials credentials = new X509CertificateCredentials(new X509Certificate[] {INVALID_CERTIFICATE});
        
        assertFalse(this.authenticationHandler.authenticate(credentials));
    }
    
    public void testValidCertificate() throws Exception {
        final X509CertificateCredentials credentials = new X509CertificateCredentials(new X509Certificate[] {VALID_CERTIFICATE});
        
        assertTrue(this.authenticationHandler.authenticate(credentials));
    }
    
    public void testValidCertificateWithInvalidFirst() throws Exception {
        final X509CertificateCredentials credentials = new X509CertificateCredentials(new X509Certificate[] {INVALID_CERTIFICATE, VALID_CERTIFICATE});
        
        assertTrue(this.authenticationHandler.authenticate(credentials));
    }
    
    public void testValidCertificateWithNotTrustedIssuer() throws Exception {
        ((X509CredentialsAuthenticationHandler) authenticationHandler).setTrustedIssuers(new String[] {"test"});
        final X509CertificateCredentials credentials = new X509CertificateCredentials(new X509Certificate[] {VALID_CERTIFICATE});
        
        assertFalse(this.authenticationHandler.authenticate(credentials));
    }
}
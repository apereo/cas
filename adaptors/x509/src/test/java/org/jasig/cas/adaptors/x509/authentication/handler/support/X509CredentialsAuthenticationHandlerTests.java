/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.x509.authentication.handler.support;

import java.security.cert.X509Certificate;

import org.jasig.cas.adaptors.x509.authentication.handler.support.X509CredentialsAuthenticationHandler;
import org.jasig.cas.adaptors.x509.authentication.principal.AbstractX509CertificateTests;
import org.jasig.cas.adaptors.x509.authentication.principal.X509CertificateCredentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 *
 */
public class X509CredentialsAuthenticationHandlerTests extends AbstractX509CertificateTests {
    private X509CredentialsAuthenticationHandler authenticationHandler;

    protected void setUp() throws Exception {
        this.authenticationHandler = new X509CredentialsAuthenticationHandler();
        this.authenticationHandler.setTrustedIssuerDnPattern("JA-SIG");
        this.authenticationHandler.afterPropertiesSet();        
    }
    
    public void testSupportsClass() {
        assertTrue(this.authenticationHandler.supports(new X509CertificateCredentials(new X509Certificate[0])));
    }
    
    public void testDoesntSupportClass() {
        assertFalse(this.authenticationHandler.supports(new UsernamePasswordCredentials()));
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
        this.authenticationHandler.setTrustedIssuerDnPattern("test");
        this.authenticationHandler.afterPropertiesSet();
        final X509CertificateCredentials credentials = new X509CertificateCredentials(new X509Certificate[] {VALID_CERTIFICATE});
        
        assertFalse(this.authenticationHandler.authenticate(credentials));
    }
    
    public void testValidCertificateWithCustomDistinguishedDn() throws Exception {
        this.authenticationHandler.setSubjectDnPattern("s.*");
        this.authenticationHandler.afterPropertiesSet();
        final X509CertificateCredentials credentials = new X509CertificateCredentials(new X509Certificate[] {VALID_CERTIFICATE});
        
        assertTrue(this.authenticationHandler.authenticate(credentials));
    }
}
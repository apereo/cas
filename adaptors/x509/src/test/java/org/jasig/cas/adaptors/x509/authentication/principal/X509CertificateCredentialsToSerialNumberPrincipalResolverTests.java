/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.x509.authentication.principal;

import java.security.cert.X509Certificate;

import org.jasig.cas.adaptors.x509.authentication.principal.X509CertificateCredentials;
import org.jasig.cas.adaptors.x509.authentication.principal.X509CertificateCredentialsToDistinguishedNamePrincipalResolver;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

public class X509CertificateCredentialsToSerialNumberPrincipalResolverTests
    extends AbstractX509CertificateTests {

    private X509CertificateCredentialsToDistinguishedNamePrincipalResolver resolver = new X509CertificateCredentialsToDistinguishedNamePrincipalResolver();
    
    public void testGetDistinguishedName() {
        final X509CertificateCredentials c = new X509CertificateCredentials(new X509Certificate[] {VALID_CERTIFICATE});
        c.setCertificate(VALID_CERTIFICATE);
        
        assertEquals(VALID_CERTIFICATE.getSubjectDN().getName(), this.resolver.resolvePrincipal(c).getId());
    }
    
    public void testSupport() {
        final X509CertificateCredentials c = new X509CertificateCredentials(new X509Certificate[] {VALID_CERTIFICATE});
        
        assertTrue(this.resolver.supports(c));
    }
    
    public void testSupportFalse() {
        assertFalse(this.resolver.supports(new UsernamePasswordCredentials()));
    }
    
}

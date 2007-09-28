/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.x509.authentication.principal;

import java.security.cert.X509Certificate;

import org.jasig.cas.adaptors.x509.authentication.principal.X509CertificateCredentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

/**
 * @author Scott Battaglia
 * @author Jan Van der Velpen
 * @version $Revision$ $Date$
 * @since 3.0.6
 *
 */
public class X509CertificateCredentialsToSNAndIssuerDNPrincipalResolverTests
    extends AbstractX509CertificateTests {
    
    private X509CertificateCredentialsToSerialNumberAndIssuerDNPrincipalResolver resolver = new X509CertificateCredentialsToSerialNumberAndIssuerDNPrincipalResolver();
    
    public void testResolvePrincipalInternal() {
        final X509CertificateCredentials c = new X509CertificateCredentials(new X509Certificate[] {VALID_CERTIFICATE});
        c.setCertificate(VALID_CERTIFICATE);
        
        
        final String value = "SERIALNUMBER=" + VALID_CERTIFICATE.getSerialNumber().toString() + ", " + VALID_CERTIFICATE.getIssuerDN().getName();
        
        assertEquals(value, this.resolver.resolvePrincipal(c).getId());
    }
    
    public void testSupport() {
        final X509CertificateCredentials c = new X509CertificateCredentials(new X509Certificate[] {VALID_CERTIFICATE});
        assertTrue(this.resolver.supports(c));
    }
    
    public void testSupportFalse() {
        assertFalse(this.resolver.supports(new UsernamePasswordCredentials()));
    }
    
}

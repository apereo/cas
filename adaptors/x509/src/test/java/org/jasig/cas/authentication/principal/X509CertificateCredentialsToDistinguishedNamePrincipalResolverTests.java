/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import java.security.cert.X509Certificate;


public class X509CertificateCredentialsToDistinguishedNamePrincipalResolverTests
    extends AbstractX509CertificateTests {

    private X509CertificateCredentialsToSerialNumberPrincipalResolver resolver = new X509CertificateCredentialsToSerialNumberPrincipalResolver();
    
    public void testGetDistinguishedName() {
        final X509CertificateCredentials c = new X509CertificateCredentials(new X509Certificate[] {VALID_CERTIFICATE});
        c.setCertificate(VALID_CERTIFICATE);
        
        assertEquals(VALID_CERTIFICATE.getSerialNumber().toString(), this.resolver.resolvePrincipal(c).getId());
    }   
}

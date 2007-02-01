/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.x509.authentication.principal;

import java.security.cert.X509Certificate;

import org.jasig.cas.adaptors.x509.authentication.principal.X509CertificateCredentials;
import org.jasig.cas.adaptors.x509.authentication.principal.X509CertificateCredentialsToSerialNumberAndIssuerDNPrincipalResolver;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;


/**
 * @author Jan Van der Velpen
 * @version $Revision$ $Date$
 * @since 3.0.6
 *
 */
public class X509CertificateCredentialsToSerialNumberAndIssuerDNPrincipalResolverTests
    extends AbstractX509CertificateTests {

    private X509CertificateCredentialsToSerialNumberAndIssuerDNPrincipalResolver resolver = new X509CertificateCredentialsToSerialNumberAndIssuerDNPrincipalResolver();
    private final String SERIALNUMBERPREFIX = "TSTSERIAL=";
    private final String VALUEDELIMITER = ", TST=tst,";
    
    public void testResolvePrincipalInternal() throws Exception {
        final X509CertificateCredentials c = new X509CertificateCredentials(new X509Certificate[] {VALID_CERTIFICATE});
        c.setCertificate(VALID_CERTIFICATE);

        this.resolver.setSerialNumberPrefix(this.SERIALNUMBERPREFIX);
        this.resolver.setValueDelimiter(this.VALUEDELIMITER);
        this.resolver.afterPropertiesSet();

        assertEquals("The principals should match: ", 
            this.resolver.resolvePrincipal(c).getId(),
            this.SERIALNUMBERPREFIX
                + c.getCertificate().getSerialNumber().toString()
                + this.VALUEDELIMITER
                + c.getCertificate().getIssuerDN().getName()
            );
    }
        
    public void testSupport() {
        final X509CertificateCredentials c = new X509CertificateCredentials(new X509Certificate[] {VALID_CERTIFICATE});   
        assertTrue(this.resolver.supports(c));
    }
    
    public void testSupportFalse() {
        assertFalse(this.resolver.supports(new UsernamePasswordCredentials()));
    }
}

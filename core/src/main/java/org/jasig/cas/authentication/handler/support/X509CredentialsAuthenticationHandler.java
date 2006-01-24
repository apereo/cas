/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import java.security.Principal;
import java.security.cert.X509Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.X509CertificateCredentials;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Authentication Handler that accepts X509 Certificiates, determines their
 * validity and ensures that they were issued by a trusted issuer.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public class X509CredentialsAuthenticationHandler implements
    AuthenticationHandler, InitializingBean {

    /** Instance of Logging. */
    private final Log log = LogFactory.getLog(getClass());

    /** The list of trusted issuers. */
    private String[] trustedIssuers;

    public boolean authenticate(final Credentials credentials)
        throws AuthenticationException {
        final X509CertificateCredentials x509Credentials = (X509CertificateCredentials) credentials;
        final X509Certificate[] certificates = x509Credentials
            .getCertificates();

        for (int i = 0; i < certificates.length; i++) {
            final X509Certificate certificate = certificates[i];
            try {
                certificate.checkValidity();
                final Principal principal = certificate.getIssuerDN();

                if (principal != null
                    && isCertificateFromTrustedIssuer(principal)) {
                    x509Credentials.setCertificate(certificate);
                    if (log.isDebugEnabled()) {
                        log.debug("Trusted Issuer [" + principal.getName()
                            + "] found for certificate issued for ["
                            + certificate.getSerialNumber().toString() + "]");
                    }
                    return true;
                }

            } catch (final Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn("Certficiate [" + certificate + "] expired.");
                }
            }
        }

        return false;
    }

    public void setTrustedIssuers(final String[] trustedIssuers) {
        this.trustedIssuers = trustedIssuers;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notEmpty(this.trustedIssuers);
    }

    private boolean isCertificateFromTrustedIssuer(final Principal principal) {
        for (int j = 0; j < this.trustedIssuers.length; j++) {
            if (principal.getName().equals(this.trustedIssuers[j])) {
                return true;
            }
        }

        return false;
    }

    public boolean supports(final Credentials credentials) {
        return credentials != null
            && X509CertificateCredentials.class.isAssignableFrom(credentials
                .getClass());
    }
}

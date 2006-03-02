/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.X509CertificateCredentials;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Authentication Handler that accepts X509 Certificiates, determines their
 * validity and ensures that they were issued by a trusted issuer.
 * <p>
 * Deployers can supply an optional pattern to match subject dns against to
 * further restrict certificates in case they are not using their own issuer.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public final class X509CredentialsAuthenticationHandler implements
    AuthenticationHandler, InitializingBean {

    /** Default subject pattern match. */
    private static final String DEFAULT_SUBJECT_DN_PATTERN = ".*";

    /** Instance of Logging. */
    private final Log log = LogFactory.getLog(getClass());

    /** The list of trusted issuers. */
    private String trustedIssuer;

    /** Deployer supplied pattern to match subject DNs against. */
    private String subjectDnPattern;

    /** The compiled pattern supplied by the deployer. */
    private Pattern regExSubjectDnPattern;

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
                    && isCertificateFromTrustedIssuer(principal)
                    && doesCertificateSubjectDnMatchPattern(certificate
                        .getSubjectDN())) {
                    x509Credentials.setCertificate(certificate);
                    if (log.isDebugEnabled()) {
                        log.debug("Trusted Issuer [" + principal.getName()
                            + "] found for certificate issued for ["
                            + certificate.getSerialNumber().toString() + "]");
                    }
                    return true;
                }
                
                if (log.isDebugEnabled()) {
                    log.debug("Trusted Issuer [" + principal.getName()
                        + "] not found for certificate issued for ["
                        + certificate.getSerialNumber().toString() + "]");
                }
            } catch (final Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn("Certficiate [" + certificate + "] expired.");
                }
            }
        }

        return false;
    }

    public void setTrustedIssuer(final String trustedIssuer) {
        this.trustedIssuer = trustedIssuer;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.trustedIssuer);

        if (!StringUtils.hasText(this.subjectDnPattern)) {
            log.info("Using default Subject DN Pattern: "
                + DEFAULT_SUBJECT_DN_PATTERN);
            this.subjectDnPattern = DEFAULT_SUBJECT_DN_PATTERN;
        }

        this.regExSubjectDnPattern = Pattern.compile(this.subjectDnPattern);
    }

    public void setSubjectDnPattern(final String subjectDnPattern) {
        this.subjectDnPattern = subjectDnPattern;
    }

    private boolean isCertificateFromTrustedIssuer(final Principal principal) {
        return (principal.getName().equals(this.trustedIssuer));
    }

    private boolean doesCertificateSubjectDnMatchPattern(
        final Principal principal) {
        final boolean result = this.regExSubjectDnPattern.matcher(
            principal.getName()).matches();

        if (log.isDebugEnabled()) {
            log.debug("Attempted to pattern match [" + principal.getName()
                + "] against [" + this.subjectDnPattern + "].  Result: "
                + result);
        }
        return result;
    }

    public boolean supports(final Credentials credentials) {
        return credentials != null
            && X509CertificateCredentials.class.isAssignableFrom(credentials
                .getClass());
    }
}

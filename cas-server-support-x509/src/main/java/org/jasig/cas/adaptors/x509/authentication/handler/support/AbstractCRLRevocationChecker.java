/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.x509.authentication.handler.support;

import java.security.GeneralSecurityException;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.adaptors.x509.util.CertUtils;

/**
 * Base class for all CRL-based revocation checkers.
 *
 * @author Marvin S. Addison
 * @version $Revision$
 * @since 3.4.6
 *
 */
public abstract class AbstractCRLRevocationChecker implements RevocationChecker {
    /** Logger instance */
    protected final Log log = LogFactory.getLog(getClass());

    /** Policy to apply when CRL data is unavailable. */
    @NotNull
    private RevocationPolicy<Void> unavailableCRLPolicy = new DenyRevocationPolicy();

    /** Policy to apply when CRL data has expired. */
    @NotNull
    private RevocationPolicy<X509CRL> expiredCRLPolicy = new ThresholdExpiredCRLRevocationPolicy();


    /** {@inheritDoc} */
    public void check(final X509Certificate cert) throws GeneralSecurityException {
        if (cert == null) {
            throw new IllegalArgumentException("Certificate cannot be null.");
        }
        if (log.isDebugEnabled()) {
	        log.debug("Evaluating certificate revocation status for " + CertUtils.toString(cert));
        }
        final X509CRL crl = getCRL(cert);
        if (crl == null) {
            log.warn("CRL data is not available for " + CertUtils.toString(cert));
            this.unavailableCRLPolicy.apply(null);
            return;
        }
        if (CertUtils.isExpired(crl)) {
            log.warn("CRL data expired on " + crl.getNextUpdate());
            this.expiredCRLPolicy.apply(crl);
        }
        final X509CRLEntry entry = crl.getRevokedCertificate(cert);
        if (entry != null) {
            throw new RevokedCertificateException(entry);
        }
    }
    
    /**
     * Sets the policy to apply when CRL data is unavailable.
     *
     * @param policy Revocation policy.
     */
    public void setUnavailableCRLPolicy(final RevocationPolicy<Void> policy) {
        this.unavailableCRLPolicy = policy;
    }
    
    /**
     * Sets the policy to apply when CRL data is expired.
     *
     * @param policy Revocation policy.
     */
    public void setExpiredCRLPolicy(final RevocationPolicy<X509CRL> policy) {
        this.expiredCRLPolicy = policy;
    }
   
    /**
     * Gets the CRL for the given certificate.
     *
     * @param cert Certificate for which the CRL of the issuing CA should be retrieved.
     * 
     * @return CRL for given cert.
     */
    protected abstract X509CRL getCRL(final X509Certificate cert);

}

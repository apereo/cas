/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.adaptors.x509.authentication.handler.support;

import java.security.GeneralSecurityException;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.jasig.cas.adaptors.x509.util.CertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all CRL-based revocation checkers.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 *
 */
public abstract class AbstractCRLRevocationChecker implements RevocationChecker {
    /** Logger instance. **/
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Flag to indicate whether all
     * crls should be checked for the cert resource.
     * Defaults to <code>false</code>.
     **/
    protected boolean checkAll;

    /** Policy to apply when CRL data is unavailable. */
    @NotNull
    private RevocationPolicy<Void> unavailableCRLPolicy = new DenyRevocationPolicy();

    /** Policy to apply when CRL data has expired. */
    @NotNull
    private RevocationPolicy<X509CRL> expiredCRLPolicy = new ThresholdExpiredCRLRevocationPolicy();

    /**
     * {@inheritDoc}
     **/
    @Override
    public void check(final X509Certificate cert) throws GeneralSecurityException {
        if (cert == null) {
            throw new IllegalArgumentException("Certificate cannot be null.");
        }
        logger.debug("Evaluating certificate revocation status for {}", CertUtils.toString(cert));
        final Collection<X509CRL> crls = getCRLs(cert);

        if (crls == null || crls.isEmpty()) {
            logger.warn("CRL data is not available for {}", CertUtils.toString(cert));
            this.unavailableCRLPolicy.apply(null);
            return;
        }

        final List<X509CRL> expiredCrls = new ArrayList<>();
        final List<X509CRLEntry> revokedCrls = new ArrayList<>();

        final Iterator<X509CRL> it = crls.iterator();
        while (it.hasNext())  {
            final X509CRL crl = it.next();
            if (CertUtils.isExpired(crl)) {
                logger.warn("CRL data expired on {}", crl.getNextUpdate());
                expiredCrls.add(crl);
            }
        }

        if (crls.size() == expiredCrls.size()) {
            logger.warn("All CRLs retrieved have expired. Applying CRL expiration policy...");
            for (final X509CRL crl : expiredCrls) {
                this.expiredCRLPolicy.apply(crl);
            }
        } else {
            crls.removeAll(expiredCrls);
            logger.debug("Valid CRLs [{}] found that are not expired yet", crls);

            for (final X509CRL crl : crls) {
                final X509CRLEntry entry = crl.getRevokedCertificate(cert);
                if (entry != null) {
                    revokedCrls.add(entry);
                }
            }

            if (revokedCrls.size() == crls.size()) {
                final X509CRLEntry entry = revokedCrls.get(0);
                logger.warn("All CRL entries have been revoked. Rejecting the first entry [{}]", entry);
                throw new RevokedCertificateException(entry);
            }
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
     * Indicates whether all resources should be checked,
     * or revocation should stop at the first resource
     * that produces the cert.
     *
     * @param checkAll the check all
     */
    public final void setCheckAll(final boolean checkAll) {
        this.checkAll = checkAll;
    }

    /**
     * Gets the first fetched CRL for the given certificate.
     *
     * @param cert Certificate for which the CRL of the issuing CA should be retrieved.
     *
     * @return CRL for given cert, or null
     */
    public final X509CRL getCRL(final X509Certificate cert) {
        final Collection<X509CRL> list = getCRLs(cert);
        if (list != null && !list.isEmpty()) {
            return list.iterator().next();
        }
        logger.debug("No CRL could be found for {}", CertUtils.toString(cert));
        return null;
    }

    /**
     * Records the addition of a new CRL entry.
     * @param id the id of the entry to keep track of
     * @param crl new CRL entry
     * @return true if the entry was added successfully.
     * @since 4.1
     */
    protected abstract boolean addCRL(final Object id, final X509CRL crl);

    /**
     * Gets the collection of CRLs for the given certificate.
     *
     * @param cert Certificate for which the CRL of the issuing CA should be retrieved.
     * @return CRLs for given cert.
     */
    protected abstract Collection<X509CRL> getCRLs(final X509Certificate cert);
}

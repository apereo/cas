package org.apereo.cas.adaptors.x509.authentication.revocation.checker;

import org.apereo.cas.adaptors.x509.authentication.revocation.RevokedCertificateException;
import org.apereo.cas.adaptors.x509.authentication.revocation.policy.DenyRevocationPolicy;
import org.apereo.cas.adaptors.x509.authentication.revocation.policy.RevocationPolicy;
import org.apereo.cas.adaptors.x509.authentication.revocation.policy.ThresholdExpiredCRLRevocationPolicy;
import org.apereo.cas.util.crypto.CertUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Base class for all CRL-based revocation checkers.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 */
public abstract class AbstractCRLRevocationChecker implements RevocationChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCRLRevocationChecker.class);

    /**
     * Flag to indicate whether all
     * crls should be checked for the cert resource.
     * Defaults to {@code false}.
     **/
    protected final boolean checkAll;

    /**
     * Policy to apply when CRL data is unavailable.
     */
    private final RevocationPolicy<Void> unavailableCRLPolicy;

    /**
     * Policy to apply when CRL data has expired.
     */
    private final RevocationPolicy<X509CRL> expiredCRLPolicy;

    /**
     * Instantiates a new Abstract crl revocation checker.
     *
     * @param checkAll             Indicates whether all resources should be checked,
     *                             or revocation should stop at the first resource
     *                             that produces the cert.
     * @param unavailableCRLPolicy the unavailable crl policy
     * @param expiredCRLPolicy     the expired crl policy
     */
    public AbstractCRLRevocationChecker(final boolean checkAll,
                                        final RevocationPolicy<Void> unavailableCRLPolicy,
                                        final RevocationPolicy<X509CRL> expiredCRLPolicy) {
        this.checkAll = checkAll;
        this.unavailableCRLPolicy = unavailableCRLPolicy == null
                ? new DenyRevocationPolicy() : unavailableCRLPolicy;
        this.expiredCRLPolicy = expiredCRLPolicy == null
                ? new ThresholdExpiredCRLRevocationPolicy(0) : expiredCRLPolicy;
    }

    @Override
    public void check(final X509Certificate cert) throws GeneralSecurityException {
        if (cert == null) {
            throw new IllegalArgumentException("Certificate cannot be null.");
        }
        LOGGER.debug("Evaluating certificate revocation status for [{}]", CertUtils.toString(cert));
        final Collection<X509CRL> crls = getCRLs(cert);

        if (crls == null || crls.isEmpty()) {
            LOGGER.warn("CRL data is not available for [{}]", CertUtils.toString(cert));
            this.unavailableCRLPolicy.apply(null);
            return;
        }

        final List<X509CRL> expiredCrls = new ArrayList<>();
        final List<X509CRLEntry> revokedCrls;

        crls.stream().filter(CertUtils::isExpired).forEach(crl -> {
            LOGGER.warn("CRL data expired on [{}]", crl.getNextUpdate());
            expiredCrls.add(crl);
        });

        if (crls.size() == expiredCrls.size()) {
            LOGGER.warn("All CRLs retrieved have expired. Applying CRL expiration policy...");
            for (final X509CRL crl : expiredCrls) {
                this.expiredCRLPolicy.apply(crl);
            }
        } else {
            crls.removeAll(expiredCrls);
            LOGGER.debug("Valid CRLs [{}] found that are not expired yet", crls);

            revokedCrls = crls.stream().map(crl -> crl.getRevokedCertificate(cert)).filter(Objects::nonNull).collect(Collectors.toList());

            if (revokedCrls.size() == crls.size()) {
                final X509CRLEntry entry = revokedCrls.get(0);
                LOGGER.warn("All CRL entries have been revoked. Rejecting the first entry [{}]", entry);
                throw new RevokedCertificateException(entry);
            }
        }
    }

    public RevocationPolicy<Void> getUnavailableCRLPolicy() {
        return this.unavailableCRLPolicy;
    }

    public RevocationPolicy<X509CRL> getExpiredCRLPolicy() {
        return this.expiredCRLPolicy;
    }

    /**
     * Records the addition of a new CRL entry.
     *
     * @param id  the id of the entry to keep track of
     * @param crl new CRL entry
     * @return true if the entry was added successfully.
     * @since 4.1
     */
    protected abstract boolean addCRL(Object id, X509CRL crl);

    /**
     * Gets the collection of CRLs for the given certificate.
     *
     * @param cert Certificate for which the CRL of the issuing CA should be retrieved.
     * @return CRLs for given cert.
     */
    protected abstract Collection<X509CRL> getCRLs(X509Certificate cert);
}

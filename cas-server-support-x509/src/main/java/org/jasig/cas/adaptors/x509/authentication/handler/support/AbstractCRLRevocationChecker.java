package org.jasig.cas.adaptors.x509.authentication.handler.support;

import java.security.GeneralSecurityException;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.jasig.cas.adaptors.x509.util.CertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Base class for all CRL-based revocation checkers.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 *
 */
public abstract class AbstractCRLRevocationChecker implements RevocationChecker {
    /** Logger instance. **/
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Flag to indicate whether all
     * crls should be checked for the cert resource.
     * Defaults to {@code false}.
     **/
    protected boolean checkAll;

    /** Policy to apply when CRL data is unavailable. */
    private RevocationPolicy<Void> unavailableCRLPolicy;

    /** Policy to apply when CRL data has expired. */
    private RevocationPolicy<X509CRL> expiredCRLPolicy;


    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        if (this.unavailableCRLPolicy == null){
           this.unavailableCRLPolicy = new DenyRevocationPolicy();
        }
        if (this.expiredCRLPolicy == null) {
            this.expiredCRLPolicy = new ThresholdExpiredCRLRevocationPolicy();
        }
    }

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


    public RevocationPolicy<Void> getUnavailableCRLPolicy() {
        return unavailableCRLPolicy;
    }

    public RevocationPolicy<X509CRL> getExpiredCRLPolicy() {
        return expiredCRLPolicy;
    }

    /**
     * Indicates whether all resources should be checked,
     * or revocation should stop at the first resource
     * that produces the cert.
     *
     * @param checkAll the check all
     */
    @Autowired
    public void setCheckAll(@Value("${cas.x509.authn.crl.checkAll:false}")
                                final boolean checkAll) {
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
    protected abstract boolean addCRL(Object id, X509CRL crl);

    /**
     * Gets the collection of CRLs for the given certificate.
     *
     * @param cert Certificate for which the CRL of the issuing CA should be retrieved.
     * @return CRLs for given cert.
     */
    protected abstract Collection<X509CRL> getCRLs(X509Certificate cert);
}

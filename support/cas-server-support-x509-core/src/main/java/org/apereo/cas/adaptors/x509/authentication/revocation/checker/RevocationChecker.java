package org.apereo.cas.adaptors.x509.authentication.revocation.checker;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;


/**
 * Strategy interface for checking revocation status of a certificate.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 *
 */
@FunctionalInterface
public interface RevocationChecker {
    /**
     * Checks the revocation status of the given certificate.
     *
     * @param certificate Certificate to examine.
     *
     * @throws GeneralSecurityException If certificate has been revoked or the revocation
     * check fails for some reason such as revocation data not available.
     */
    void check(X509Certificate certificate) throws GeneralSecurityException;
}

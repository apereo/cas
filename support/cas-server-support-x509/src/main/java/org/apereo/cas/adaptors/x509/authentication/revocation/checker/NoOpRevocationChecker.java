package org.apereo.cas.adaptors.x509.authentication.revocation.checker;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

/**
 * NO-OP implementation certificate revocation checker.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 */
public class NoOpRevocationChecker implements RevocationChecker {

    /**
     * NO-OP check implementation.
     *
     * @param certificate Certificate to check.
     *
     * @throws GeneralSecurityException Never thrown.
     *
     * @see RevocationChecker#check(java.security.cert.X509Certificate)
     */
    @Override
    public void check(final X509Certificate certificate) throws GeneralSecurityException {
        // NO-OP
    }
}

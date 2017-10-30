package org.apereo.cas.adaptors.x509.authentication.revocation.checker;

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
     *
     */
    @Override
    public void check(final X509Certificate certificate) {
        // NO-OP
    }
}

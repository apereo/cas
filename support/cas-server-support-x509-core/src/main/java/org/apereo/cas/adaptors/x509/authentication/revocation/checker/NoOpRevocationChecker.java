package org.apereo.cas.adaptors.x509.authentication.revocation.checker;

import lombok.extern.slf4j.Slf4j;

import java.security.cert.X509Certificate;

/**
 * NO-OP implementation certificate revocation checker.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 */
@Slf4j
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

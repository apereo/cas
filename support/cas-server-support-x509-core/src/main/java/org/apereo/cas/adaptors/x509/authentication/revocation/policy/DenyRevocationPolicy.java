package org.apereo.cas.adaptors.x509.authentication.revocation.policy;

import java.security.GeneralSecurityException;


/**
 * Implements a deny policy by throwing an exception.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 *
 */
public class DenyRevocationPolicy implements RevocationPolicy<Void> {

    /**
     * Policy application throws GeneralSecurityException to stop execution of
     * whatever process invoked application of this policy.
     *
     * @param nothing SHOULD be null; ignored in all cases.
     *
     * @throws GeneralSecurityException Thrown in all cases.
     *
     */
    @Override
    public void apply(final Void nothing) throws GeneralSecurityException {
        throw new GeneralSecurityException("Aborting since DenyRevocationPolicy is in effect.");
    }
}

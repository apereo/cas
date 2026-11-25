package org.apereo.cas.adaptors.x509.authentication.revocation.policy;

import module java.base;


/**
 * Implements a deny policy by throwing an exception, applicable to unavailable and expired CRL data alike.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 */
public class DenyRevocationPolicy implements RevocationPolicy<Object> {

    /**
     * Policy application throws GeneralSecurityException to stop execution of
     * whatever process invoked application of this policy.
     *
     * @param data the unavailable (null) or expired CRL data; ignored in all cases.
     * @throws GeneralSecurityException Thrown in all cases.
     */
    @Override
    public void apply(final Object data) throws GeneralSecurityException {
        throw new GeneralSecurityException("Aborting since DenyRevocationPolicy is in effect.");
    }
}

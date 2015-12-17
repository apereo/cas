package org.jasig.cas.adaptors.x509.authentication.handler.support;

import java.security.GeneralSecurityException;


/**
 * Strategy interface for enforcing various policy matters related to certificate
 * revocation, such as what to do when revocation data is unavailable or stale.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 *
 */
public interface RevocationPolicy<T> {
    /**
     * Applies the policy.
     *
     * @param data Data to help make a decision according to policy.
     *
     * @throws GeneralSecurityException When policy application poses a security
     * risk or policy application is prevented for security reasons.
     */
    void apply(T data) throws GeneralSecurityException;
}

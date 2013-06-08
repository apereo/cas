package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.Credentials;

/**
 * Adapts a CAS 4.0 {@link Credential} onto a CAS 3.x {@link Credentials}.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public interface CredentialsAdapter {
    /**
     * Converts a CAS 4.0 credential to a CAS 3.0 credential.
     *
     * @return CAS 3.0 credential.
     */
    Credentials convert(Credential credential);
}

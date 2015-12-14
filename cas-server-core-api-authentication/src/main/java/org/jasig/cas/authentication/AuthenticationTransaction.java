package org.jasig.cas.authentication;

import java.util.Collection;

/**
 * This is {@link AuthenticationTransaction}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public interface AuthenticationTransaction {

    /**
     * Gets credentials.
     *
     * @return the credentials
     */
    Collection<Credential> getCredentials();
}

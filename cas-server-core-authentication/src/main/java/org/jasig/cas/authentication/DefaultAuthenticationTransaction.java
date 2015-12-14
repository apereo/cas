package org.jasig.cas.authentication;

import java.util.Collection;

/**
 * This is {@link DefaultAuthenticationTransaction}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
class DefaultAuthenticationTransaction implements AuthenticationTransaction {
    private final Collection<Credential> credentials;

    /**
     * Instantiates a new Default authentication transaction.
     *
     * @param credentials the credentials
     */
    DefaultAuthenticationTransaction(final Collection<Credential> credentials) {
        this.credentials = credentials;
    }

    @Override
    public Collection<Credential> getCredentials() {
        return this.credentials;
    }
}

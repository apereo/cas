package org.jasig.cas.authentication;

/**
 * This is {@link AuthenticationTransactionFactory}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public interface AuthenticationTransactionFactory {
    /**
     * Get authentication transaction.
     *
     * @param credentials the credentials
     * @return the authentication transaction
     */
    AuthenticationTransaction get(Credential... credentials);
}

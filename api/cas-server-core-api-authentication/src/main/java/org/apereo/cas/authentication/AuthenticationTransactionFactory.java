package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;

import java.io.Serializable;

/**
 * This is {@link AuthenticationTransactionFactory},
 * which produces {@link AuthenticationTransaction} objects
 * that may contain one more or credentials.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@FunctionalInterface
public interface AuthenticationTransactionFactory extends Serializable {

    /**
     * Default bean name.
     */
    String BEAN_NAME = "authenticationTransactionFactory";

    /**
     * New transaction.
     *
     * @param service     the service
     * @param credentials the credentials
     * @return the authentication transaction
     */
    AuthenticationTransaction newTransaction(Service service, Credential... credentials);

    /**
     * New transaction.
     *
     * @param credentials the credentials
     * @return the authentication transaction
     */
    default AuthenticationTransaction newTransaction(final Credential... credentials) {
        return newTransaction(null, credentials);
    }
}

package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link AuthenticationTransaction}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public interface AuthenticationTransaction extends Serializable {

    /**
     * Gets service linked to this transaction.
     *
     * @return the service
     */
    Service getService();

    /**
     * Gets credentials.
     *
     * @return the credentials
     */
    Collection<Credential> getCredentials();

    /**
     * Gets the first (primary) credential in the chain.
     *
     * @return the credential
     */
    default Optional<Credential> getPrimaryCredential() {
        return getCredentials().stream().findFirst();
    }

    /**
     * Does this AuthenticationTransaction contain a credential of the given type?
     *
     * @param type the credential type to check for
     * @return true if this AuthenticationTransaction contains a credential of the specified type
     */
    default boolean hasCredentialOfType(final Class<? extends Credential> type) {
        return getCredentials().stream().anyMatch(type::isInstance);
    }

    /**
     * Gets credentials of type.
     *
     * @param <T>  the type parameter
     * @param type the type
     * @return the credentials of type
     */
    default <T extends Credential> Collection<T> getCredentialsOfType(final Class<T> type) {
        return getCredentials().stream().filter(type::isInstance).map(c -> (T) c).collect(Collectors.toList());
    }
}


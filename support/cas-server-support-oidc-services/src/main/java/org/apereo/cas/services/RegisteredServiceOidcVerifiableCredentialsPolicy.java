package org.apereo.cas.services;

import module java.base;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is {@link RegisteredServiceOidcVerifiableCredentialsPolicy}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceOidcVerifiableCredentialsPolicy extends Serializable {
    /**
     * Gets allowed credential types.
     *
     * @return the allowed credential types
     */
    Set<String> getAllowedCredentialTypes();

    /**
     * Gets credential signing alg values supported.
     *
     * @return the credential signing alg values supported
     */
    Set<String> getCredentialSigningAlgValuesSupported();

    /**
     * Is the given credential type allowed for this service.
     * <p>
     * A policy that names no credential type at all is not a policy that forbids everything; it is a
     * policy that has nothing to say, which leaves the service with whatever the issuer offers. Only a
     * policy that actually lists types narrows the service down to those types.
     *
     * @param credentialType the credential configuration id
     * @return true if the service may obtain this credential type
     */
    default boolean isCredentialTypeAllowed(final String credentialType) {
        return getAllowedCredentialTypes() == null
            || getAllowedCredentialTypes().isEmpty()
            || getAllowedCredentialTypes().contains(credentialType);
    }
}

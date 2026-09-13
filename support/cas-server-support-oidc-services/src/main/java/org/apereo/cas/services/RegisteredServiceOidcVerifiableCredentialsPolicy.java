package org.apereo.cas.services;

import module java.base;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is {@link RegisteredServiceOidcVerifiableCredentialsPolicy}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceOidcVerifiableCredentialsPolicy extends Serializable {
    /**
     * Gets allowed credential types.
     *
     * @return the allowed credential types
     */
    Set<String> getAllowedCredentialTypes();
}

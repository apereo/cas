package org.apereo.cas.authentication;

/**
 * Interface for credentials created by MFA providers to match up which provider created them.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
public interface MultifactorAuthenticationCredential {

    /**
     * Returns the unique id of the provider that created the credential.
     *
     * @return - the mark
     */
    default String getProviderId() {
        return null;
    }

    /**
     * Sets the unique mark of the provider that created the credential.
     *
     * @param providerId - the id
     */
    default void setProviderId(final String providerId) {
    }

}

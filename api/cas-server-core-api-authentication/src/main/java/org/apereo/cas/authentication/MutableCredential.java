package org.apereo.cas.authentication;

/**
 * This is {@link MutableCredential}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface MutableCredential extends Credential {
    /**
     * Sets id.
     *
     * @param id the id
     */
    default void setId(final String id) {
    }

    /**
     * Sets credential metadata.
     *
     * @param credentialMetadata the credential metadata
     */
    default void setCredentialMetadata(final CredentialMetadata credentialMetadata) {
    }
}

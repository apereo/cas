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
    void setId(String id);

    /**
     * Sets credential metadata.
     *
     * @param credentialMetadata the credential metadata
     */
    void setCredentialMetadata(CredentialMetadata credentialMetadata);
}

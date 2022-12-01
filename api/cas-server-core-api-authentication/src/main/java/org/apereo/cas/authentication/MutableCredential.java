package org.apereo.cas.authentication;

/**
 * This is {@link MutableCredential}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface MutableCredential extends Credential {
    void setId(String id);

    void setCredentialMetadata(CredentialMetadata credentialMetadata);
}

package org.apereo.cas.authentication;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * Describes an authentication credential. Implementations SHOULD also implement {@link CredentialMetadata} if
 * no sensitive data is contained in the credential; conversely, implementations MUST NOT implement
 * {@link CredentialMetadata} if the credential contains sensitive data (e.g. password, key material).
 *
 * @author William G. Thompson, Jr.
 * @author Marvin S. Addison
 * @see CredentialMetadata
 * @since 3.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface Credential extends Serializable {
    /**
     * Credential type, collected as metadata for authentication.
     */
    String CREDENTIAL_TYPE_ATTRIBUTE = "credentialType";

    /**
     * An ID that may be used to indicate the credential identifier is unknown.
     */
    String UNKNOWN_ID = "unknown";

    /**
     * Gets a credential identifier that is safe to record for logging, auditing, or presentation to the user.
     * In most cases this has a natural meaning for most credential types (e.g. username, certificate DN), while
     * for others it may be awkward to construct a meaningful identifier. In any case credentials require some means
     * of identification for a number of cases and implementers should make a best effort to satisfy that need.
     *
     * @return Non-null credential identifier. Implementers should return {@link #UNKNOWN_ID} for cases where an ID
     * is not readily available or meaningful.
     */
    String getId();

    /**
     * Gets credential metadata.
     *
     * @return the credential metadata
     */
    CredentialMetadata getCredentialMetadata();
}

package org.apereo.cas.authentication;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Describes a credential provided for authentication. Implementations should expect instances of this type to be
 * stored for periods of time equal to the length of the SSO session or longer, which necessitates consideration of
 * serialization and security. All implementations MUST be serializable and secure with respect to long-term storage.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY)
public interface CredentialMetaData {
    /**
     * Gets a unique identifier for the kind of credential this represents.
     *
     * @return Unique identifier for the given type of credential.
     */
    String getId();

    /**
     * Gets the type of the original credential.
     *
     * @return Non-null credential class.
     */
    Class<? extends Credential> getCredentialClass();
}

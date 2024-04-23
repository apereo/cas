package org.apereo.cas.authentication;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

/**
 * Describes a credential provided for authentication. Implementations should expect instances of this type to be
 * stored for periods of time equal to the length of the SSO session or longer, which necessitates consideration of
 * serialization and security. All implementations MUST be serializable and secure with respect to long-term storage.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface CredentialMetadata extends Serializable {
    /**
     * User agent property linked to this credential.
     */
    String PROPERTY_USER_AGENT = "UserAgent";

    /**
     * Geo location property linked to this credential.
     */
    String PROPERTY_GEO_LOCATION = "GeoLocation";

    /**
     * Gets a unique identifier for the kind of credential this represents.
     *
     * @return Unique identifier for the given type of credential.
     */
    String getId();

    /**
     * Gets the type of the original credential.
     *
     * @return Non -null credential class.
     */
    Class<? extends Credential> getCredentialClass();

    /**
     * Add trait.
     *
     * @param credentialTrait the credential trait
     */
    CredentialMetadata addTrait(CredentialTrait credentialTrait);

    /**
     * Remove trait.
     *
     * @param trait the trait
     */
    CredentialMetadata removeTrait(Class<? extends CredentialTrait> trait);

    /**
     * Gets trait.
     *
     * @param <T>   the type parameter
     * @param trait the trait
     * @return the trait
     */
    <T extends CredentialTrait> Optional<T> getTrait(Class<T> trait);

    /**
     * Gets properties.
     *
     * @return the properties
     */
    Map<String, Serializable> getProperties();

    /**
     * Put properties.
     *
     * @param properties the properties
     */
    CredentialMetadata putProperties(Map<String, ? extends Serializable> properties);

    /**
     * Put property credential metadata.
     *
     * @param key   the key
     * @param value the value
     * @return the credential metadata
     */
    CredentialMetadata putProperty(String key, Serializable value);

    /**
     * Contains property?.
     *
     * @param key the key
     * @return true or false
     */
    boolean containsProperty(String key);
}

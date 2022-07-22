package org.apereo.cas.authentication;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Map;

/**
 * This is {@link DetailedCredentialMetaData}.
 * Carries additional properties and details for a credential metadata.
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface DetailedCredentialMetaData extends CredentialMetaData {
    /**
     * User agent property linked to this credential.
     */
    String PROPERTY_USER_AGENT = "UserAgent";
    /**
     * Geo location property linked to this credential.
     */
    String PROPERTY_GEO_LOCATION = "GeoLocation";

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
    void putProperties(Map<String, Serializable> properties);
}

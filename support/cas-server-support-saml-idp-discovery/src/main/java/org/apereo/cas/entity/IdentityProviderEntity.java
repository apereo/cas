package org.apereo.cas.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link IdentityProviderEntity}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Data
public class IdentityProviderEntity implements Serializable {
    private static final long serialVersionUID = -8825733147579229506L;

    @JsonProperty("Descriptions")
    private final List<IdentityProviderBasicEntity> descriptions = new ArrayList<>();

    @JsonProperty("Logos")
    private final List<IdentityProviderLogoEntity> logos = new ArrayList<>();

    @JsonProperty("DisplayNames")
    private final List<IdentityProviderBasicEntity> displayNames = new ArrayList<>();

    @JsonProperty("PrivacyStatementURLs")
    private final List<IdentityProviderBasicEntity> privacyStatementUrls = new ArrayList<>();

    @JsonProperty("InformationURLs")
    private final List<IdentityProviderBasicEntity> informationUrls = new ArrayList<>();

    @JsonProperty("Keywords")
    private final List<IdentityProviderBasicEntity> keywords = new ArrayList<>();

    private String entityID;

}

package org.apereo.cas.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link SamlIdentityProviderEntity}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@Setter
@EqualsAndHashCode(of = "entityID")
public class SamlIdentityProviderEntity implements Serializable, Comparable<SamlIdentityProviderEntity> {
    private static final long serialVersionUID = -8825733147579229506L;

    @JsonProperty("Descriptions")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<SamlIdentityProviderBasicEntity> descriptions = new ArrayList<>(0);

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("Logos")
    private final List<SamlIdentityProviderLogoEntity> logos = new ArrayList<>(0);

    @JsonProperty("DisplayNames")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<SamlIdentityProviderBasicEntity> displayNames = new ArrayList<>(0);

    @JsonProperty("PrivacyStatementURLs")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<SamlIdentityProviderBasicEntity> privacyStatementUrls = new ArrayList<>(0);

    @JsonProperty("InformationURLs")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<SamlIdentityProviderBasicEntity> informationUrls = new ArrayList<>(0);

    @JsonProperty("Keywords")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<SamlIdentityProviderBasicEntity> keywords = new ArrayList<>(0);

    private String entityID;

    @Override
    public int compareTo(final SamlIdentityProviderEntity o) {
        return new CompareToBuilder()
            .append(entityID, o.entityID)
            .build();
    }
}

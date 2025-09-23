package org.apereo.cas.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import jakarta.annotation.Nonnull;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
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
    @Serial
    private static final long serialVersionUID = -8825733147579229506L;

    @JsonProperty("Descriptions")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<SamlIdentityProviderBasicEntity> descriptions = new ArrayList<>();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("Logos")
    private final List<SamlIdentityProviderLogoEntity> logos = new ArrayList<>();

    @JsonProperty("DisplayNames")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<SamlIdentityProviderBasicEntity> displayNames = new ArrayList<>();

    @JsonProperty("PrivacyStatementURLs")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<SamlIdentityProviderBasicEntity> privacyStatementUrls = new ArrayList<>();

    @JsonProperty("InformationURLs")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<SamlIdentityProviderBasicEntity> informationUrls = new ArrayList<>();

    @JsonProperty("Keywords")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<SamlIdentityProviderBasicEntity> keywords = new ArrayList<>();

    private String entityID;

    @Override
    public int compareTo(@Nonnull final SamlIdentityProviderEntity entity) {
        return Comparator.comparing(SamlIdentityProviderEntity::getEntityID).compare(this, entity);
    }
}

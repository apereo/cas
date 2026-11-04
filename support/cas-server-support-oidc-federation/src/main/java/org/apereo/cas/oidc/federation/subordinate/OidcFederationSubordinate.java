package org.apereo.cas.oidc.federation.subordinate;

import module java.base;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import tools.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;

/**
 * This is {@link OidcFederationSubordinate}.
 *
 * @author Jerome LELEU
 * @since 8.0.0
 */
@ToString(exclude = {"metadata", "metadataPolicy", "federationKeys"})
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Accessors(chain = true)
public class OidcFederationSubordinate {

    @NotBlank
    private String entityId;

    private JsonNode metadata;

    /**
     * The metadata policy this entity sets for the subordinate, by entity type (OpenID Federation 1.0, section 6.1).
     */
    private JsonNode metadataPolicy;

    /**
     * The non-standard metadata policy operators that parties must understand to use the metadata policy.
     */
    private List<String> metadataPolicyCrit = new ArrayList<>();

    private JsonNode federationKeys;

    /**
     * The constraints this entity sets for the subordinate and the entities below it (OpenID Federation 1.0,
     * section 6.2), published as they are in the subordinate statement.
     */
    private JsonNode constraints;

    /**
     * The entity types of the subordinate, by which the subordinate listing endpoint filters (OpenID Federation 1.0,
     * section 8.2). When not defined, the entity types are those of the {@code metadata} of the definition: a metadata
     * policy for a type does not mean the subordinate has it.
     */
    private List<String> entityTypes = new ArrayList<>();

    private String description;
}

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
@ToString(exclude = {"metadata", "federationKeys"})
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Accessors(chain = true)
public class OidcFederationSubordinate {

    @NotBlank
    private String entityId;

    private JsonNode metadata;

    private JsonNode federationKeys;

    private String description;
}

package org.apereo.cas.oidc.federation.service;

import module java.base;
import org.apereo.cas.services.BaseWebBasedRegisteredService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import tools.jackson.databind.JsonNode;

/**
 * This is {@link OidcFederationEntityService}.
 *
 * @author Jerome LELEU
 * @since 8.0.0
 */
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class OidcFederationEntityService extends BaseWebBasedRegisteredService {
    /**
     * Friendly name for this OIDC federation entity service.
     */
    public static final String OIDC_FEDERATION_ENTITY_SERVICE_FRIENDLY_NAME = "OpenID Connect Federation Entity";

    @Serial
    private static final long serialVersionUID = 7786340912407613562L;

    private JsonNode metadata;

    private JsonNode federationKeys;

    @JsonIgnore
    @Override
    public int getEvaluationPriority() {
        return 1;
    }

    @JsonIgnore
    @Override
    public String getFriendlyName() {
        return OIDC_FEDERATION_ENTITY_SERVICE_FRIENDLY_NAME;
    }
}

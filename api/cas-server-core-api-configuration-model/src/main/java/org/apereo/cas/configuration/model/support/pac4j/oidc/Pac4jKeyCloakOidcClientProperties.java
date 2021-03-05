package org.apereo.cas.configuration.model.support.pac4j.oidc;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link Pac4jKeyCloakOidcClientProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("Pac4jKeyCloakOidcClientProperties")
public class Pac4jKeyCloakOidcClientProperties extends BasePac4jOidcClientProperties {
    private static final long serialVersionUID = 3209382317533639638L;

    /**
     * Keycloak realm used to construct metadata discovery URI.
     */
    @RequiredProperty
    private String realm;

    /**
     * Keycloak base URL used to construct metadata discovery URI.
     */
    @RequiredProperty
    private String baseUri;
}

package org.apereo.cas.configuration.model.support.pac4j.oidc;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link Pac4jKeyCloakOidcClientProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
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

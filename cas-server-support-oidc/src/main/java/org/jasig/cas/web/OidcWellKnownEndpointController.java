package org.jasig.cas.web;

import com.google.common.collect.ImmutableSet;
import org.jasig.cas.OidcConstants;
import org.jasig.cas.config.ServerDiscoveryProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is {@link OidcWellKnownEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RestController("oidcWellknownController")
public class OidcWellKnownEndpointController {

    @Value("${server.prefix}")
    private String serverPrefix;

    @Value("${cas.oidc.issuer:http://localhost:8080/cas/oidc}")
    private String issuer;

    /**
     * Gets well known discovery configuration.
     *
     * @return the well known discovery configuration
     */
    @RequestMapping(value = '/' + OidcConstants.BASE_OIDC_URL + "/.well-known", method = RequestMethod.GET)
    public ServerDiscoveryProperties getWellKnownDiscoveryConfiguration() {

        final ServerDiscoveryProperties discoveryProperties =
                new ServerDiscoveryProperties(this.serverPrefix, this.issuer);

        discoveryProperties.setSupportedClaims(OidcConstants.CLAIMS);
        discoveryProperties.setSupportedScopes(OidcConstants.SCOPES);

        discoveryProperties.setSupportedResponseTypes(ImmutableSet.of("code", "token"));
        discoveryProperties.setSupportedSubjectTypes(ImmutableSet.of("public", "pairwise"));
        discoveryProperties.setSupportedClaimTypes(ImmutableSet.of("normal"));

        discoveryProperties.setSupportedGrantTypes(ImmutableSet.of("authorization_code", "password", "implicit"));
        return discoveryProperties;
    }

    /**
     * Gets well known openid discovery configuration.
     *
     * @return the well known discovery configuration
     */
    @RequestMapping(value = '/' + OidcConstants.BASE_OIDC_URL + "/.well-known/openid-configuration", method = RequestMethod.GET)
    public ServerDiscoveryProperties getWellKnownOpenIdDiscoveryConfiguration() {
        return getWellKnownDiscoveryConfiguration();
    }
}

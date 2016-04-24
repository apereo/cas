package org.jasig.cas.web;

import com.google.common.collect.ImmutableList;
import org.jasig.cas.OidcConstants;
import org.jasig.cas.config.ServerDiscoveryProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is {@link WellKnownEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RestController("wellknownController")
public class WellKnownEndpointController {

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

        discoveryProperties.setSupportedClaims(
                ImmutableList.of("sub", "name", "preferred_username",
                        "family_name", "given_name", "middle_name", "given_name", "profile",
                        "picture", "nickname", "website", "zoneinfo", "locale", "updated_at",
                        "birthdate", "email", "email_verified", "phone_number",
                        "phone_number_verified", "address"));
        discoveryProperties.setSupportedScopes(
                ImmutableList.of("openid", "profile", "email", "address", "phone", "offline_access"));

        discoveryProperties.setSupportedResponseTypes(ImmutableList.of("code", "token"));
        discoveryProperties.setSupportedSubjectTypes(ImmutableList.of("public", "pairwise"));
        discoveryProperties.setSupportedClaimTypes(ImmutableList.of("normal"));

        discoveryProperties.setSupportedGrantTypes(ImmutableList.of("authorization_code", "password", "implicit"));
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

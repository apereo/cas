package org.apereo.cas.web;

import com.google.common.collect.ImmutableList;
import org.apereo.cas.OidcConstants;
import org.apereo.cas.config.OidcServerDiscoverySettings;
import org.apereo.cas.configuration.model.core.ServerProperties;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * This is {@link OidcWellKnownEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RestController("wellknownController")
public class OidcWellKnownEndpointController {

    @Resource
    private OidcProperties properties;

    @Resource
    private ServerProperties serverProperties;

    /**
     * Gets well known discovery configuration.
     *
     * @return the well known discovery configuration
     */
    @RequestMapping(value = '/' + OidcConstants.BASE_OIDC_URL + "/.well-known", method = RequestMethod.GET)
    public OidcServerDiscoverySettings getWellKnownDiscoveryConfiguration() {

        final OidcServerDiscoverySettings discoveryProperties =
                new OidcServerDiscoverySettings(this.serverProperties.getPrefix(), properties.getIssuer());

        discoveryProperties.setSupportedClaims(
                ImmutableList.of(OidcConstants.CLAIM_SUB, "name", OidcConstants.CLAIM_PREFERRED_USERNAME,
                        "family_name", "given_name", "middle_name", "given_name", "profile",
                        "picture", "nickname", "website", "zoneinfo", "locale", "updated_at",
                        "birthdate", "email", "email_verified", "phone_number",
                        "phone_number_verified", "address"));
        discoveryProperties.setSupportedScopes(OidcConstants.SCOPES);

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
    public OidcServerDiscoverySettings getWellKnownOpenIdDiscoveryConfiguration() {
        return getWellKnownDiscoveryConfiguration();
    }
}

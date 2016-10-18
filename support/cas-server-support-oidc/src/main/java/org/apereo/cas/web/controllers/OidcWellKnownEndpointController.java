package org.apereo.cas.web.controllers;

import com.google.common.collect.ImmutableList;
import org.apereo.cas.OidcConstants;
import org.apereo.cas.OidcServerDiscoverySettings;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.web.BaseOAuthWrapperController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * This is {@link OidcWellKnownEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcWellKnownEndpointController extends BaseOAuthWrapperController {

    @Autowired
    private CasConfigurationProperties casProperties;

    /**
     * Gets well known discovery configuration.
     *
     * @return the well known discovery configuration
     */
    @RequestMapping(value = '/' + OidcConstants.BASE_OIDC_URL + "/.well-known", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OidcServerDiscoverySettings> getWellKnownDiscoveryConfiguration() {

        final OidcServerDiscoverySettings discoveryProperties =
                new OidcServerDiscoverySettings(casProperties.getServer().getPrefix(), casProperties.getAuthn().getOidc().getIssuer());

        discoveryProperties.setClaimsSupported(
                ImmutableList.of(OidcConstants.CLAIM_SUB, "name", OidcConstants.CLAIM_PREFERRED_USERNAME,
                        "family_name", "given_name", "middle_name", "given_name", "profile",
                        "picture", "nickname", "website", "zoneinfo", "locale", "updated_at",
                        "birthdate", "email", "email_verified", "phone_number",
                        "phone_number_verified", "address"));
        discoveryProperties.setScopesSupported(OidcConstants.SCOPES);

        discoveryProperties.setResponseTypesSupported(ImmutableList.of("code", "token"));
        discoveryProperties.setSubjectTypesSupported(ImmutableList.of("public", "pairwise"));
        discoveryProperties.setClaimTypesSupported(ImmutableList.of("normal"));

        discoveryProperties.setGrantTypesSupported(ImmutableList.of("authorization_code", "password", "implicit"));

        discoveryProperties.setIdTokenSigningAlgValuesSupported(ImmutableList.of("none", "RS256"));

        return new ResponseEntity(discoveryProperties, HttpStatus.OK);
    }

    /**
     * Gets well known openid discovery configuration.
     *
     * @return the well known discovery configuration
     */
    @RequestMapping(value = '/' + OidcConstants.BASE_OIDC_URL + "/.well-known/openid-configuration",
            method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OidcServerDiscoverySettings> getWellKnownOpenIdDiscoveryConfiguration() {
        return getWellKnownDiscoveryConfiguration();
    }
}

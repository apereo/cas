package org.apereo.cas.web.controllers;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This is {@link OidcWellKnownEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcWellKnownEndpointController extends BaseOAuthWrapperController {

    private static final List<String> CLAIMS_SUPPORTED = Arrays.asList(OidcConstants.CLAIM_SUB, "name", OidcConstants.CLAIM_PREFERRED_USERNAME,
            "family_name", "given_name", "middle_name", "given_name", "profile", "picture", "nickname", "website", "zoneinfo",
            "locale", "updated_at", "birthdate", "email", "email_verified", "phone_number", "phone_number_verified", "address");
    private static final List<String> RESPONSE_TYPES_SUPPORTED = Arrays.asList("code", "token");
    private static final List<String> SUBJECT_RESPONSE_TYPES = Arrays.asList("public", "pairwise");
    private static final List<String> CLAIM_TYPES_SUPPORTED = Collections.singletonList("normal");
    private static final List<String> GRANT_TYPES_SUPPORTED = Arrays.asList("authorization_code", "password", "implicit");
    private static final List<String> TOKEN_VALUES_SUPPORTED = Arrays.asList("none", "RS256");

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

        discoveryProperties.setClaimsSupported(CLAIMS_SUPPORTED);
        discoveryProperties.setScopesSupported(OidcConstants.SCOPES);
        discoveryProperties.setResponseTypesSupported(RESPONSE_TYPES_SUPPORTED);
        discoveryProperties.setSubjectTypesSupported(SUBJECT_RESPONSE_TYPES);
        discoveryProperties.setClaimTypesSupported(CLAIM_TYPES_SUPPORTED);
        discoveryProperties.setGrantTypesSupported(GRANT_TYPES_SUPPORTED);
        discoveryProperties.setIdTokenSigningAlgValuesSupported(TOKEN_VALUES_SUPPORTED);

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

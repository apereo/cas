package org.apereo.cas.oidc.web.controllers.discovery;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.oidc.discovery.webfinger.OidcWebFingerDiscoveryService;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * This is {@link OidcWellKnownEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcWellKnownEndpointController extends BaseOAuth20Controller {

    private final OidcWebFingerDiscoveryService webFingerDiscoveryService;

    public OidcWellKnownEndpointController(final OAuth20ConfigurationContext oAuthConfigurationContext,
                                           final OidcWebFingerDiscoveryService webFingerDiscoveryService) {
        super(oAuthConfigurationContext);
        this.webFingerDiscoveryService = webFingerDiscoveryService;
    }

    /**
     * Gets well known discovery configuration.
     *
     * @return the well known discovery configuration
     */
    @GetMapping(value = '/' + OidcConstants.BASE_OIDC_URL + "/.well-known", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OidcServerDiscoverySettings> getWellKnownDiscoveryConfiguration() {
        return new ResponseEntity(this.webFingerDiscoveryService.getDiscovery(), HttpStatus.OK);
    }

    /**
     * Gets well known openid discovery configuration.
     *
     * @return the well known discovery configuration
     */
    @GetMapping(value = '/' + OidcConstants.BASE_OIDC_URL + "/.well-known/openid-configuration", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OidcServerDiscoverySettings> getWellKnownOpenIdDiscoveryConfiguration() {
        return getWellKnownDiscoveryConfiguration();
    }

    /**
     * Gets web finger response.
     *
     * @param resource the resource
     * @param rel      the rel
     * @return the web finger response
     */
    @GetMapping(value = '/' + OidcConstants.BASE_OIDC_URL + "/.well-known/webfinger", produces = "application/jrd+json")
    public ResponseEntity getWebFingerResponse(@RequestParam("resource") final String resource,
                                               @RequestParam(value = "rel", required = false) final String rel) {
        return webFingerDiscoveryService.handleWebFingerDiscoveryRequest(resource, rel);
    }
}

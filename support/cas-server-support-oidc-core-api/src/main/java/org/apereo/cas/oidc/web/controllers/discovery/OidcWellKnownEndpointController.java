package org.apereo.cas.oidc.web.controllers.discovery;

import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.oidc.discovery.webfinger.OidcWebFingerDiscoveryService;
import org.apereo.cas.oidc.web.controllers.BaseOidcController;
import org.apereo.cas.util.spring.beans.BeanSupplier;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * This is {@link OidcWellKnownEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Tag(name = "OpenID Connect")
public class OidcWellKnownEndpointController extends BaseOidcController {

    private final OidcWebFingerDiscoveryService webFingerDiscoveryService;

    public OidcWellKnownEndpointController(final OidcConfigurationContext configurationContext,
                                           final OidcWebFingerDiscoveryService webFingerDiscoveryService) {
        super(configurationContext);
        this.webFingerDiscoveryService = webFingerDiscoveryService;
    }

    /**
     * Gets well known discovery configuration.
     *
     * @param request  the request
     * @param response the response
     * @return the well known discovery configuration
     */
    @GetMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.WELL_KNOWN_URL,
        "/**/" + OidcConstants.WELL_KNOWN_URL
    }, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handle OIDC discovery request",
        description = "Handles requests for well-known OIDC discovery configuration")
    public ResponseEntity<OidcServerDiscoverySettings> getWellKnownDiscoveryConfiguration(final HttpServletRequest request,
                                                                                          final HttpServletResponse response) {
        return getOidcServerDiscoveryResponse(request, response, OidcConstants.WELL_KNOWN_URL);
    }

    /**
     * Gets well known openid discovery configuration.
     *
     * @param request  the request
     * @param response the response
     * @return the well known discovery configuration
     */
    @GetMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.WELL_KNOWN_OPENID_CONFIGURATION_URL,
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.WELL_KNOWN_OAUTH_AUTHORIZATION_SERVER_URL,
        "/**/" + OidcConstants.WELL_KNOWN_OPENID_CONFIGURATION_URL}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handle OIDC discovery request",
        description = "Handles requests for well-known OIDC discovery configuration")
    public ResponseEntity<OidcServerDiscoverySettings> getWellKnownOpenIdDiscoveryConfiguration(final HttpServletRequest request,
                                                                                                final HttpServletResponse response) {
        return getOidcServerDiscoveryResponse(request, response, OidcConstants.WELL_KNOWN_OPENID_CONFIGURATION_URL);
    }

    /**
     * Gets web finger response.
     *
     * @param resource the resource
     * @param rel      the rel
     * @return the web finger response
     * @throws Throwable the throwable
     */
    @GetMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.WELL_KNOWN_URL + "/webfinger",
        produces = "application/jrd+json")
    @Operation(summary = "Handle webfinger discovery request")
    public ResponseEntity<Map> getWebFingerResponse(
        @RequestParam("resource")
        final String resource,
        @RequestParam(value = "rel", required = false)
        final String rel) throws Throwable {
        return BeanSupplier.isNotProxy(webFingerDiscoveryService)
            ? webFingerDiscoveryService.handleRequest(resource, rel)
            : ResponseEntity.notFound().build();
    }

    private ResponseEntity<OidcServerDiscoverySettings> getOidcServerDiscoveryResponse(final HttpServletRequest request,
                                                                                       final HttpServletResponse response,
                                                                                       final String endpoint) {
        if (isIssuerValidForEndpoint(request, response, endpoint) && BeanSupplier.isNotProxy(webFingerDiscoveryService)) {
            val discovery = webFingerDiscoveryService.getDiscovery();
            return new ResponseEntity<>(discovery, HttpStatus.OK);
        }
        LOGGER.warn("Unable to accept request; issuer for endpoint [{}] is invalid", endpoint);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}

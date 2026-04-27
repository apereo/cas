package org.apereo.cas.oidc.federation.web;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.service.OidcFederationEntityService;
import org.apereo.cas.oidc.federation.signature.OidcFederationEntityStatementService;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationRole.isTaOrIntermediate;

/**
 * This is {@link OidcTrustAnchorFetchEndpointController}.
 *
 * @author Jerome LELEU
 * @since 8.0.0
 */
@Slf4j
public class OidcTrustAnchorFetchEndpointController extends AbstractOidcFederationEndpointController {

    private final ServicesManager servicesManager;

    public OidcTrustAnchorFetchEndpointController(final ServicesManager servicesManager, final OidcIssuerService oidcIssuerService,
                                                   final OidcFederationEntityStatementService federationEntityStatementService,
                                                   final OidcProperties oidcProperties) {
        super(oidcIssuerService, federationEntityStatementService, oidcProperties);
        this.servicesManager = servicesManager;
    }

    /**
     * Gets the entity statement for the requested entity.
     *
     * @param sub the entityId
     * @param request  the request
     * @param response the response
     * @return the specific entity statement
     */
    @GetMapping('/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.FETCH_FEDERATION_URL)
    @Operation(summary = "Handle OIDC fetch federation request",
        description = "Handles requests for the fetch federation endpoint",
        parameters = {
            @Parameter(name = "sub", description = "entityId", required = true)
        })
    public ResponseEntity fetchEntityStatement(@RequestParam(value = "sub", required = false) final String sub,
        final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        LOGGER.info("Building entity statement for subordinate: [{}]", sub);

        val role = oidcProperties.getFederation().getRole();
        if (!isTaOrIntermediate(role)) {
            throw new IllegalArgumentException("Federation role [" + role + "] is not supported for Trust Anchor/Intermediate");
        }

        val error = retrieveInvalidIssuerError(request, response, OidcConstants.FETCH_FEDERATION_URL);
        if (error != null) {
            return error;
        }

        if (StringUtils.isBlank(sub)) {
            val body = OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "Invalid entity");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        val requestedService = searchService(sub);
        if (requestedService == null) {
            val body = OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "Invalid entity");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }
        val serviceMetadata = requestedService.getMetadata();
        if (serviceMetadata == null) {
            throw new IllegalArgumentException("No metadata defined for entity");
        }
        val federationKeys = requestedService.getFederationKeys();
        if (federationKeys == null || federationKeys.isEmpty()) {
            throw new IllegalArgumentException("No federation keys defined for entity");
        }

        val issuer = oidcProperties.getCore().getIssuer();
        val metadata = (JSONObject) JSONValue.parse(serviceMetadata.toString());
        return buildEntityStatement(issuer, sub, metadata, federationKeys, null);
    }

    protected OidcFederationEntityService searchService(final String sub) {
        val oidcServices = servicesManager.getAllServicesOfType(OidcFederationEntityService.class);
        for (val service : oidcServices) {
            if (service.getServiceId().equals(sub)) {
                return service;
            }
        }
        return null;
    }
}

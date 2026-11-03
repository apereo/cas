package org.apereo.cas.oidc.federation.web;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.signature.OidcFederationEntityStatementService;
import org.apereo.cas.oidc.federation.subordinate.OidcFederationSubordinateRepository;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcFetchFederationEndpointController}.
 *
 * @author Jerome LELEU
 * @since 8.0.0
 */
@Slf4j
public class OidcFetchFederationEndpointController extends AbstractOidcFederationEndpointController {
    private static final String NOT_FOUND = "not_found";

    private static final String SERVER_ERROR = "server_error";

    private final OidcFederationSubordinateRepository subordinateRepository;

    public OidcFetchFederationEndpointController(final OidcFederationSubordinateRepository subordinateRepository, final OidcIssuerService oidcIssuerService,
                                                 final OidcFederationEntityStatementService federationEntityStatementService,
                                                 final OidcProperties oidcProperties) {
        super(oidcIssuerService, federationEntityStatementService, oidcProperties);
        this.subordinateRepository = subordinateRepository;
    }

    /**
     * Gets the subordinate statement about the requested entity (OpenID Federation 1.0, section 8.1).
     * Errors are JSON (section 8.9): an entity that is not a subordinate is {@code not_found} with 404,
     * a missing {@code sub} or this entity itself is {@code invalid_request} with 400, a role that issues no
     * subordinate statements answers 404, and a subordinate defined without metadata or keys is
     * {@code server_error} with 500.
     *
     * @param sub      the entityId
     * @param request  the request
     * @param response the response
     * @return the specific entity statement
     * @throws Exception the exception
     */
    @GetMapping('/' + OidcConstants.BASE_OIDC_URL + OidcConstants.FETCH_FEDERATION_URL)
    @Operation(summary = "Handle OIDC fetch federation request",
        description = "Handles requests for the fetch federation endpoint",
        parameters = {
            @Parameter(name = "sub", description = "entityId", required = true)
        })
    public ResponseEntity fetchEntityStatement(@RequestParam(value = "sub", required = false) final String sub,
        final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        LOGGER.info("Building entity statement for subordinate: [{}]", sub);

        val role = oidcProperties.getFederation().getRole();
        if (!role.isTrustAnchorOrIntermediate()) {
            return errorResponse(HttpStatus.NOT_FOUND, NOT_FOUND, "Federation role [" + role + "] does not issue subordinate statements");
        }

        val error = retrieveInvalidIssuerError(request, response, OidcConstants.FETCH_FEDERATION_URL);
        if (error != null) {
            return error;
        }

        val issuer = oidcProperties.getCore().getIssuer();
        if (StringUtils.isBlank(sub) || Strings.CS.equals(sub, issuer)) {
            return errorResponse(HttpStatus.BAD_REQUEST, OAuth20Constants.INVALID_REQUEST, "Invalid entity");
        }

        val foundSubordinate = subordinateRepository.getSubordinates().get(sub);
        if (foundSubordinate == null) {
            return errorResponse(HttpStatus.NOT_FOUND, NOT_FOUND, "Unknown subordinate entity");
        }
        val serviceMetadata = foundSubordinate.getMetadata();
        val federationKeys = foundSubordinate.getFederationKeys();
        if (serviceMetadata == null || federationKeys == null || federationKeys.isEmpty()) {
            LOGGER.error("Subordinate [{}] must define its metadata and federation keys", sub);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, SERVER_ERROR, "Subordinate entity is not defined correctly");
        }

        val metadata = (JSONObject) JSONValue.parse(serviceMetadata.toString());
        return buildEntityStatement(issuer, sub, metadata, federationKeys, null);
    }

    private static ResponseEntity errorResponse(final HttpStatus status, final String error, final String description) {
        return new ResponseEntity<>(OAuth20Utils.getErrorResponseBody(error, description), status);
    }
}

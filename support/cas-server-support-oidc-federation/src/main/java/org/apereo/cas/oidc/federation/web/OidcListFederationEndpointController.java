package org.apereo.cas.oidc.federation.web;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.signature.OidcFederationEntityStatementService;
import org.apereo.cas.oidc.federation.subordinate.OidcFederationSubordinateRepository;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcListFederationEndpointController}.
 *
 * @author Jerome LELEU
 * @since 8.0.0
 */
@Slf4j
public class OidcListFederationEndpointController extends AbstractOidcFederationEndpointController {

    private static final List<String> UNSUPPORTED_PARAMETERS = Arrays.asList("entity_type", "trust_marked", "trust_mark_type", "intermediate");

    private final OidcFederationSubordinateRepository subordinateRepository;

    public OidcListFederationEndpointController(final OidcFederationSubordinateRepository subordinateRepository, final OidcIssuerService oidcIssuerService,
                                                final OidcFederationEntityStatementService federationEntityStatementService,
                                                final OidcProperties oidcProperties) {
        super(oidcIssuerService, federationEntityStatementService, oidcProperties);
        this.subordinateRepository = subordinateRepository;
    }

    /**
     * Gets the list of subordinates.
     *
     * @param request  the request
     * @param response the response
     * @return the list of subordinates
     */
    @GetMapping('/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.LIST_FEDERATION_URL)
    @Operation(summary = "Handle OIDC list federation request",
        description = "Handles requests for the list federation endpoint")
    public ResponseEntity listSubordinates(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        LOGGER.info("Listing subordinates");

        val error = retrieveInvalidIssuerError(request, response, OidcConstants.LIST_FEDERATION_URL);
        if (error != null) {
            return error;
        }

        for (val parameter : UNSUPPORTED_PARAMETERS) {
            val value = request.getParameter(parameter);
            if (value != null) {
                val body = OAuth20Utils.getErrorResponseBody(OidcConstants.UNSUPPORTED_PARAMETER, "The " + parameter + " parameter is not supported");
                return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
            }
        }

        val subordinates = subordinateRepository.getSubordinates().keySet();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(subordinates);
    }
}

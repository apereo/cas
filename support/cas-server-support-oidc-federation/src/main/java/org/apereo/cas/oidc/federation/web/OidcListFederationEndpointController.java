package org.apereo.cas.oidc.federation.web;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.signature.OidcFederationEntityStatementService;
import org.apereo.cas.oidc.federation.subordinate.OidcFederationSubordinate;
import org.apereo.cas.oidc.federation.subordinate.OidcFederationSubordinateRepository;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityType;
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

    private static final List<String> UNSUPPORTED_PARAMETERS = List.of("trust_marked", "trust_mark_type");

    private static final String ENTITY_TYPE_PARAMETER = "entity_type";

    private static final String INTERMEDIATE_PARAMETER = "intermediate";

    private static final String FEDERATION_FETCH_ENDPOINT = "federation_fetch_endpoint";

    private final OidcFederationSubordinateRepository subordinateRepository;

    public OidcListFederationEndpointController(final OidcFederationSubordinateRepository subordinateRepository, final OidcIssuerService oidcIssuerService,
                                                final OidcFederationEntityStatementService federationEntityStatementService,
                                                final OidcProperties oidcProperties) {
        super(oidcIssuerService, federationEntityStatementService, oidcProperties);
        this.subordinateRepository = subordinateRepository;
    }

    /**
     * Lists the immediate subordinates (OpenID Federation 1.0, section 8.2). Subordinates can be filtered by
     * {@code entity_type}, which may be repeated and keeps the subordinates that have any of the given entity types, as
     * their definitions declare them or, when they declare none, as the types of their metadata; a metadata policy for a
     * type does not count, and by {@code intermediate=true}, which keeps the subordinates whose {@code federation_entity} metadata
     * publishes a fetch endpoint, as intermediates must. Trust mark filters are refused with
     * {@code unsupported_parameter}.
     *
     * @param request  the request
     * @param response the response
     * @return the list of subordinates
     * @throws Exception the exception
     */
    @GetMapping('/' + OidcConstants.BASE_OIDC_URL + OidcConstants.LIST_FEDERATION_URL)
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
                return errorResponse(HttpStatus.BAD_REQUEST, OidcConstants.UNSUPPORTED_PARAMETER, "The " + parameter + " parameter is not supported");
            }
        }

        val entityTypes = Optional.ofNullable(request.getParameterValues(ENTITY_TYPE_PARAMETER)).map(List::of).orElseGet(List::of);
        val intermediatesOnly = Boolean.parseBoolean(request.getParameter(INTERMEDIATE_PARAMETER));
        val subordinates = subordinateRepository.getSubordinates().values()
            .stream()
            .filter(subordinate -> entityTypes.isEmpty() || entityTypes.stream().anyMatch(type -> hasEntityType(subordinate, type)))
            .filter(subordinate -> !intermediatesOnly || isIntermediate(subordinate))
            .map(OidcFederationSubordinate::getEntityId)
            .toList();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(subordinates);
    }

    private static boolean hasEntityType(final OidcFederationSubordinate subordinate, final String entityType) {
        if (subordinate.getEntityTypes() != null && !subordinate.getEntityTypes().isEmpty()) {
            return subordinate.getEntityTypes().contains(entityType);
        }
        return subordinate.getMetadata() != null && subordinate.getMetadata().has(entityType);
    }

    private static boolean isIntermediate(final OidcFederationSubordinate subordinate) {
        return subordinate.getMetadata() != null
            && subordinate.getMetadata().path(EntityType.FEDERATION_ENTITY.getValue()).hasNonNull(FEDERATION_FETCH_ENDPOINT);
    }
}

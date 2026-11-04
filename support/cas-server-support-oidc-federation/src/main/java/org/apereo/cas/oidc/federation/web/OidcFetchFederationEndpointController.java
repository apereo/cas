package org.apereo.cas.oidc.federation.web;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.chain.OidcFederationMetadataPolicies;
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
import org.jspecify.annotations.Nullable;
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

    private static final String METADATA_POLICY = "metadata_policy";

    private static final String CONSTRAINTS = "constraints";

    private static final String FEDERATION_ENTITY = "federation_entity";

    private static final List<String> ENTITY_CONFIGURATION_ENDPOINTS = List.of("federation_fetch_endpoint",
        "federation_list_endpoint", "federation_trust_mark_status_endpoint");

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
     * subordinate statements answers 404, and a subordinate defined without keys, or with a metadata policy that cannot
     * be read, or constraints that are not an object, is {@code server_error} with 500. The statement carries the metadata,
     * the metadata policy and the critical metadata policy operators (section 6.1) and the constraints (section 6.2) the
     * subordinate is defined with, each only when defined. The fetch, list and trust mark status endpoints are removed from
     * the {@code federation_entity} metadata, since they are only published in entity configurations (sections 8.1, 8.2
     * and 8.4): they must be known before the metadata of superiors applies.
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
        val federationKeys = foundSubordinate.getFederationKeys();
        if (federationKeys == null || federationKeys.isEmpty()) {
            LOGGER.error("Subordinate [{}] must define its federation keys", sub);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, SERVER_ERROR, "Subordinate entity is not defined correctly");
        }
        val metadata = Optional.ofNullable(foundSubordinate.getMetadata())
            .map(value -> withoutEntityConfigurationEndpoints((JSONObject) JSONValue.parse(value.toString())))
            .orElse(null);
        val policyClaims = new LinkedHashMap<String, Object>();
        if (foundSubordinate.getMetadataPolicy() != null) {
            val metadataPolicy = (JSONObject) JSONValue.parse(foundSubordinate.getMetadataPolicy().toString());
            try {
                OidcFederationMetadataPolicies.validate(metadataPolicy);
            } catch (final Exception e) {
                LOGGER.error("Metadata policy of subordinate [{}] is invalid: [{}]", sub, e.getMessage());
                return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, SERVER_ERROR, "Subordinate entity is not defined correctly");
            }
            policyClaims.put(METADATA_POLICY, metadataPolicy);
        }
        if (!foundSubordinate.getMetadataPolicyCrit().isEmpty()) {
            policyClaims.put(OidcFederationMetadataPolicies.METADATA_POLICY_CRIT, foundSubordinate.getMetadataPolicyCrit());
        }
        if (foundSubordinate.getConstraints() != null) {
            if (!foundSubordinate.getConstraints().isObject()) {
                LOGGER.error("Constraints of subordinate [{}] must be an object", sub);
                return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, SERVER_ERROR, "Subordinate entity is not defined correctly");
            }
            policyClaims.put(CONSTRAINTS, JSONValue.parse(foundSubordinate.getConstraints().toString()));
        }
        return buildEntityStatement(issuer, sub, metadata, federationKeys, null, policyClaims);
    }

    private static @Nullable JSONObject withoutEntityConfigurationEndpoints(final JSONObject metadata) {
        if (metadata.get(FEDERATION_ENTITY) instanceof final Map<?, ?> federationEntity) {
            val remaining = new JSONObject();
            federationEntity.forEach((name, value) -> remaining.put(String.valueOf(name), value));
            ENTITY_CONFIGURATION_ENDPOINTS.forEach(remaining::remove);
            if (remaining.isEmpty()) {
                metadata.remove(FEDERATION_ENTITY);
            } else {
                metadata.put(FEDERATION_ENTITY, remaining);
            }
        }
        return metadata.isEmpty() ? null : metadata;
    }

    private static ResponseEntity errorResponse(final HttpStatus status, final String error, final String description) {
        return new ResponseEntity<>(OAuth20Utils.getErrorResponseBody(error, description), status);
    }
}

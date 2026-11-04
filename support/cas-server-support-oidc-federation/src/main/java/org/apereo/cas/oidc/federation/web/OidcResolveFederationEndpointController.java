package org.apereo.cas.oidc.federation.web;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.resolve.OidcFederationEntityResolver;
import org.apereo.cas.oidc.federation.resolve.OidcFederationResolveException;
import org.apereo.cas.oidc.federation.signature.OidcFederationEntityStatementService;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The resolve endpoint of a trust anchor or an intermediate (OpenID Federation 1.0, section 8.3), without client
 * authentication: a {@code GET} with {@code sub}, one or more {@code trust_anchor} and optionally one or more
 * {@code entity_type}, answered with a signed resolve response of type {@code resolve-response+jwt}. Errors are JSON
 * (section 8.9): a missing or malformed parameter is {@code invalid_request} with 400, trust anchors this entity does
 * not trust are {@code invalid_trust_anchor} with 404, a subject without a valid trust chain to them is
 * {@code invalid_trust_chain} with 400, metadata the policies of the chain cannot apply to is {@code invalid_metadata}
 * with 400, and other roles answer {@code not_found} with 404.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
public class OidcResolveFederationEndpointController extends AbstractOidcFederationEndpointController {
    private static final String NOT_FOUND = "not_found";

    private static final String SUBJECT = "sub";

    private static final String TRUST_ANCHOR = "trust_anchor";

    private static final String ENTITY_TYPE = "entity_type";

    private final OidcFederationEntityResolver entityResolver;

    public OidcResolveFederationEndpointController(final OidcIssuerService oidcIssuerService,
                                                   final OidcFederationEntityStatementService federationEntityStatementService,
                                                   final OidcProperties oidcProperties,
                                                   final OidcFederationEntityResolver entityResolver) {
        super(oidcIssuerService, federationEntityStatementService, oidcProperties);
        this.entityResolver = entityResolver;
    }

    /**
     * Resolve the requested entity.
     *
     * @param subject      the entity identifier of the entity to resolve
     * @param trustAnchors the trust anchors to resolve the entity to
     * @param entityTypes  the entity types whose metadata is requested
     * @param request      the request
     * @param response     the response
     * @return the signed resolve response
     * @throws Exception the exception
     */
    @GetMapping('/' + OidcConstants.BASE_OIDC_URL + OidcConstants.RESOLVE_FEDERATION_URL)
    @Operation(summary = "Handle OIDC federation resolve request",
        description = "Resolves the trust chain, metadata and trust marks of an entity to a trust anchor",
        parameters = {
            @Parameter(name = SUBJECT, description = "Entity identifier of the entity to resolve", required = true),
            @Parameter(name = TRUST_ANCHOR, description = "Entity identifier of a trust anchor, repeatable", required = true),
            @Parameter(name = ENTITY_TYPE, description = "Entity type whose metadata is requested, repeatable")
        })
    public ResponseEntity resolveEntity(@RequestParam(value = SUBJECT, required = false) final String subject,
                                        @RequestParam(value = TRUST_ANCHOR, required = false) final List<String> trustAnchors,
                                        @RequestParam(value = ENTITY_TYPE, required = false) final List<String> entityTypes,
                                        final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        val role = oidcProperties.getFederation().getRole();
        if (!role.isTrustAnchorOrIntermediate()) {
            return errorResponse(HttpStatus.NOT_FOUND, NOT_FOUND, "Federation role [" + role + "] does not resolve entities");
        }
        val error = retrieveInvalidIssuerError(request, response, OidcConstants.RESOLVE_FEDERATION_URL);
        if (error != null) {
            return error;
        }
        if (StringUtils.isBlank(subject) || trustAnchors == null || trustAnchors.isEmpty()) {
            return errorResponse(HttpStatus.BAD_REQUEST, OAuth20Constants.INVALID_REQUEST, "Missing subject or trust anchor");
        }
        try {
            val resolved = entityResolver.resolve(subject, trustAnchors, Objects.requireNonNullElseGet(entityTypes, List::of));
            return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(OidcConstants.RESOLVE_RESPONSE_CONTENT_TYPE)
                .body(resolved.serialize());
        } catch (final OidcFederationResolveException e) {
            LOGGER.debug("[{}] cannot be resolved: [{}]", subject, e.getMessage());
            return errorResponse(e.getStatus(), e.getError(), e.getMessage());
        }
    }

    private static ResponseEntity errorResponse(final HttpStatus status, final String error, @Nullable final String description) {
        return new ResponseEntity<>(OAuth20Utils.getErrorResponseBody(error, description), status);
    }
}

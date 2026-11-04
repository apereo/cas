package org.apereo.cas.oidc.federation.web;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationEndpointAuthenticationModes;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.resolve.OidcFederationEntityResolver;
import org.apereo.cas.oidc.federation.resolve.OidcFederationResolveException;
import org.apereo.cas.oidc.federation.signature.OidcFederationEntityStatementService;
import org.apereo.cas.oidc.federation.subordinate.OidcFederationSubordinateClientAuthenticator;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The resolve endpoint of a trust anchor or an intermediate (OpenID Federation 1.0, section 8.3): {@code sub}, one or
 * more {@code trust_anchor} and optionally one or more {@code entity_type}, answered with a signed resolve response of
 * type {@code resolve-response+jwt}. Requests are a {@code GET} when they are not authenticated, and a {@code POST}
 * authenticated with {@code private_key_jwt} by a subordinate when client authentication is optional or required
 * (section 8.8); the response to an authenticated request is addressed to the requester with {@code aud}. Errors are
 * JSON (section 8.9): a missing or malformed parameter, or an authenticated request when client authentication is not
 * supported, is {@code invalid_request} with 400; a request that does not authenticate when it must is
 * {@code invalid_client} with 401; trust anchors this entity does not trust are {@code invalid_trust_anchor} with 404,
 * a subject without a valid trust chain to them is {@code invalid_trust_chain} with 400, metadata the policies of the
 * chain cannot apply to is {@code invalid_metadata} with 400, and other roles answer {@code not_found} with 404.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
public class OidcResolveFederationEndpointController extends AbstractOidcFederationEndpointController {
    private static final String NOT_FOUND = "not_found";

    private static final String INVALID_CLIENT = "invalid_client";

    private static final String SUBJECT = "sub";

    private static final String TRUST_ANCHOR = "trust_anchor";

    private static final String ENTITY_TYPE = "entity_type";

    private final OidcFederationEntityResolver entityResolver;

    private final OidcFederationSubordinateClientAuthenticator clientAuthenticator;

    public OidcResolveFederationEndpointController(final OidcIssuerService oidcIssuerService,
                                                   final OidcFederationEntityStatementService federationEntityStatementService,
                                                   final OidcProperties oidcProperties,
                                                   final OidcFederationEntityResolver entityResolver,
                                                   final OidcFederationSubordinateClientAuthenticator clientAuthenticator) {
        super(oidcIssuerService, federationEntityStatementService, oidcProperties);
        this.entityResolver = entityResolver;
        this.clientAuthenticator = clientAuthenticator;
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
        val error = verifyRequest(request, response);
        if (error != null) {
            return error;
        }
        if (getResolveEndpointAuthentication() == OidcFederationEndpointAuthenticationModes.REQUIRED) {
            return errorResponse(HttpStatus.UNAUTHORIZED, INVALID_CLIENT, "Client authentication is required");
        }
        return resolve(subject, trustAnchors, entityTypes, null);
    }

    /**
     * Resolve the requested entity for the subordinate that authenticates the request.
     *
     * @param subject             the entity identifier of the entity to resolve
     * @param trustAnchors        the trust anchors to resolve the entity to
     * @param entityTypes         the entity types whose metadata is requested
     * @param clientAssertionType the client assertion type
     * @param clientAssertion     the client assertion
     * @param request             the request
     * @param response            the response
     * @return the signed resolve response, addressed to the requester
     * @throws Exception the exception
     */
    @PostMapping(value = '/' + OidcConstants.BASE_OIDC_URL + OidcConstants.RESOLVE_FEDERATION_URL,
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Operation(summary = "Handle authenticated OIDC federation resolve request",
        description = "Resolves an entity to a trust anchor for the subordinate that authenticates the request",
        parameters = {
            @Parameter(name = SUBJECT, description = "Entity identifier of the entity to resolve", required = true),
            @Parameter(name = TRUST_ANCHOR, description = "Entity identifier of a trust anchor, repeatable", required = true),
            @Parameter(name = ENTITY_TYPE, description = "Entity type whose metadata is requested, repeatable"),
            @Parameter(name = OAuth20Constants.CLIENT_ASSERTION_TYPE, description = "Client assertion type", required = true),
            @Parameter(name = OAuth20Constants.CLIENT_ASSERTION, description = "Client assertion", required = true)
        })
    public ResponseEntity resolveEntityForAuthenticatedClient(
        @RequestParam(value = SUBJECT, required = false) final String subject,
        @RequestParam(value = TRUST_ANCHOR, required = false) final List<String> trustAnchors,
        @RequestParam(value = ENTITY_TYPE, required = false) final List<String> entityTypes,
        @RequestParam(value = OAuth20Constants.CLIENT_ASSERTION_TYPE, required = false) final String clientAssertionType,
        @RequestParam(value = OAuth20Constants.CLIENT_ASSERTION, required = false) final String clientAssertion,
        final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        val error = verifyRequest(request, response);
        if (error != null) {
            return error;
        }
        if (getResolveEndpointAuthentication() == OidcFederationEndpointAuthenticationModes.NONE) {
            return errorResponse(HttpStatus.BAD_REQUEST, OAuth20Constants.INVALID_REQUEST, "Client authentication is not supported");
        }
        val requester = clientAuthenticator.authenticate(clientAssertionType, clientAssertion,
            oidcIssuerService.determineIssuer(Optional.empty()));
        if (requester.isEmpty()) {
            return errorResponse(HttpStatus.UNAUTHORIZED, INVALID_CLIENT, "Client authentication failed");
        }
        return resolve(subject, trustAnchors, entityTypes, requester.get());
    }

    private ResponseEntity resolve(@Nullable final String subject, @Nullable final List<String> trustAnchors,
                                   @Nullable final List<String> entityTypes, @Nullable final String audience)
            throws Exception {
        if (StringUtils.isBlank(subject) || trustAnchors == null || trustAnchors.isEmpty()) {
            return errorResponse(HttpStatus.BAD_REQUEST, OAuth20Constants.INVALID_REQUEST, "Missing subject or trust anchor");
        }
        try {
            val resolved = entityResolver.resolve(subject, trustAnchors, Objects.requireNonNullElseGet(entityTypes, List::of), audience);
            return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(OidcConstants.RESOLVE_RESPONSE_CONTENT_TYPE)
                .body(resolved.serialize());
        } catch (final OidcFederationResolveException e) {
            LOGGER.debug("[{}] cannot be resolved: [{}]", subject, e.getMessage());
            return errorResponse(e.getStatus(), e.getError(), e.getMessage());
        }
    }

    private @Nullable ResponseEntity verifyRequest(final HttpServletRequest request, final HttpServletResponse response) {
        val role = oidcProperties.getFederation().getRole();
        if (!role.isTrustAnchorOrIntermediate()) {
            return errorResponse(HttpStatus.NOT_FOUND, NOT_FOUND, "Federation role [" + role + "] does not resolve entities");
        }
        return retrieveInvalidIssuerError(request, response, OidcConstants.RESOLVE_FEDERATION_URL);
    }

    private OidcFederationEndpointAuthenticationModes getResolveEndpointAuthentication() {
        return oidcProperties.getFederation().getResolveEndpointAuthentication();
    }
}

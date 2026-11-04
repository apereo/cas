package org.apereo.cas.oidc.federation.web;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.configuration.model.support.oidc.federation.OidcFederationEndpointAuthenticationModes;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.signature.OidcFederationEntityStatementService;
import org.apereo.cas.oidc.federation.subordinate.OidcFederationSubordinateClientAuthenticator;
import org.apereo.cas.oidc.federation.trustmark.OidcFederationTrustMarkIssuer;
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
 * The endpoints of CAS as a trust mark issuer (OpenID Federation 1.0): the trust mark endpoint (section 8.6), the
 * trust mark status endpoint (section 8.4) and the trust marked entities listing endpoint (section 8.5). The status
 * endpoint is a {@code POST} of {@code trust_mark}. The trust mark endpoint is a {@code GET} when requests are not
 * authenticated, and a {@code POST} authenticated with {@code private_key_jwt} by a subordinate when client
 * authentication is optional or required (section 8.8); an authenticated subordinate may only ask for its own trust
 * marks. Errors are JSON (section 8.9): a missing parameter, or an authenticated request when client authentication is
 * not supported, is {@code invalid_request} with 400; a request that does not authenticate when it must, or that asks
 * for the trust mark of another entity, is {@code invalid_client} with 401; an entity that does not hold the requested
 * trust mark, a trust mark this entity did not issue, or a type it does not issue is {@code not_found} with 404, as is
 * every request when this entity issues no trust marks.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
public class OidcTrustMarkFederationEndpointController extends AbstractOidcFederationEndpointController {
    private static final String NOT_FOUND = "not_found";

    private static final String INVALID_CLIENT = "invalid_client";

    private static final String TRUST_MARK_TYPE = "trust_mark_type";

    private static final String TRUST_MARK = "trust_mark";

    private static final String SUBJECT = "sub";

    private final OidcFederationTrustMarkIssuer trustMarkIssuer;

    private final OidcFederationSubordinateClientAuthenticator clientAuthenticator;

    public OidcTrustMarkFederationEndpointController(final OidcIssuerService oidcIssuerService,
                                                     final OidcFederationEntityStatementService federationEntityStatementService,
                                                     final OidcProperties oidcProperties,
                                                     final OidcFederationTrustMarkIssuer trustMarkIssuer,
                                                     final OidcFederationSubordinateClientAuthenticator clientAuthenticator) {
        super(oidcIssuerService, federationEntityStatementService, oidcProperties);
        this.trustMarkIssuer = trustMarkIssuer;
        this.clientAuthenticator = clientAuthenticator;
    }

    /**
     * Issue the trust mark of the given type to the given entity.
     *
     * @param trustMarkType the trust mark type
     * @param subject       the entity identifier of the subject
     * @param request       the request
     * @param response      the response
     * @return the trust mark
     * @throws Exception the exception
     */
    @GetMapping('/' + OidcConstants.BASE_OIDC_URL + OidcConstants.TRUST_MARK_FEDERATION_URL)
    @Operation(summary = "Handle OIDC federation trust mark request",
        description = "Issues the trust mark of the requested type to the requested entity",
        parameters = {
            @Parameter(name = TRUST_MARK_TYPE, description = "Trust mark type", required = true),
            @Parameter(name = SUBJECT, description = "Entity identifier of the subject", required = true)
        })
    public ResponseEntity issueTrustMark(@RequestParam(value = TRUST_MARK_TYPE, required = false) final String trustMarkType,
                                         @RequestParam(value = SUBJECT, required = false) final String subject,
                                         final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        val error = verifyRequest(request, response, OidcConstants.TRUST_MARK_FEDERATION_URL);
        if (error != null) {
            return error;
        }
        if (getTrustMarkEndpointAuthentication() == OidcFederationEndpointAuthenticationModes.REQUIRED) {
            return errorResponse(HttpStatus.UNAUTHORIZED, INVALID_CLIENT, "Client authentication is required");
        }
        if (StringUtils.isAnyBlank(trustMarkType, subject)) {
            return errorResponse(HttpStatus.BAD_REQUEST, OAuth20Constants.INVALID_REQUEST, "Missing trust mark type or subject");
        }
        return issue(trustMarkType, subject);
    }

    /**
     * Issue the trust mark of the given type to the subordinate that authenticates the request, which must be the
     * subject of the trust mark.
     *
     * @param trustMarkType       the trust mark type
     * @param subject             the entity identifier of the subject
     * @param clientAssertionType the client assertion type
     * @param clientAssertion     the client assertion
     * @param request             the request
     * @param response            the response
     * @return the trust mark
     * @throws Exception the exception
     */
    @PostMapping(value = '/' + OidcConstants.BASE_OIDC_URL + OidcConstants.TRUST_MARK_FEDERATION_URL,
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Operation(summary = "Handle authenticated OIDC federation trust mark request",
        description = "Issues the trust mark of the requested type to the subordinate that authenticates the request",
        parameters = {
            @Parameter(name = TRUST_MARK_TYPE, description = "Trust mark type", required = true),
            @Parameter(name = SUBJECT, description = "Entity identifier of the subject", required = true),
            @Parameter(name = OAuth20Constants.CLIENT_ASSERTION_TYPE, description = "Client assertion type", required = true),
            @Parameter(name = OAuth20Constants.CLIENT_ASSERTION, description = "Client assertion", required = true)
        })
    public ResponseEntity issueAuthenticatedTrustMark(
        @RequestParam(value = TRUST_MARK_TYPE, required = false) final String trustMarkType,
        @RequestParam(value = SUBJECT, required = false) final String subject,
        @RequestParam(value = OAuth20Constants.CLIENT_ASSERTION_TYPE, required = false) final String clientAssertionType,
        @RequestParam(value = OAuth20Constants.CLIENT_ASSERTION, required = false) final String clientAssertion,
        final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        val error = verifyRequest(request, response, OidcConstants.TRUST_MARK_FEDERATION_URL);
        if (error != null) {
            return error;
        }
        if (getTrustMarkEndpointAuthentication() == OidcFederationEndpointAuthenticationModes.NONE) {
            return errorResponse(HttpStatus.BAD_REQUEST, OAuth20Constants.INVALID_REQUEST, "Client authentication is not supported");
        }
        if (StringUtils.isAnyBlank(trustMarkType, subject)) {
            return errorResponse(HttpStatus.BAD_REQUEST, OAuth20Constants.INVALID_REQUEST, "Missing trust mark type or subject");
        }
        val requester = clientAuthenticator.authenticate(clientAssertionType, clientAssertion,
            oidcIssuerService.determineIssuer(Optional.empty()));
        if (requester.isEmpty()) {
            return errorResponse(HttpStatus.UNAUTHORIZED, INVALID_CLIENT, "Client authentication failed");
        }
        if (!requester.get().equals(subject)) {
            LOGGER.warn("Authenticated subordinate [{}] asks for the trust mark of [{}]", requester.get(), subject);
            return errorResponse(HttpStatus.UNAUTHORIZED, INVALID_CLIENT, "Authenticated requests may only ask for the trust marks of the requester");
        }
        return issue(trustMarkType, subject);
    }

    private ResponseEntity issue(final String trustMarkType, final String subject) throws Exception {
        return trustMarkIssuer.issue(trustMarkType, subject)
            .map(trustMark -> ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(OidcConstants.TRUST_MARK_CONTENT_TYPE)
                .body((Object) trustMark.serialize()))
            .orElseGet(() -> errorResponse(HttpStatus.NOT_FOUND, NOT_FOUND, "Entity does not hold a trust mark of this type"));
    }

    private OidcFederationEndpointAuthenticationModes getTrustMarkEndpointAuthentication() {
        return oidcProperties.getFederation().getTrustMarkEndpointAuthentication();
    }

    /**
     * Answer the status of a trust mark this entity issued.
     *
     * @param trustMark the trust mark
     * @param request   the request
     * @param response  the response
     * @return the signed trust mark status response
     * @throws Exception the exception
     */
    @PostMapping('/' + OidcConstants.BASE_OIDC_URL + OidcConstants.TRUST_MARK_STATUS_FEDERATION_URL)
    @Operation(summary = "Handle OIDC federation trust mark status request",
        description = "Answers the status of a trust mark issued by this entity",
        parameters = @Parameter(name = TRUST_MARK, description = "Trust mark", required = true))
    public ResponseEntity trustMarkStatus(@RequestParam(value = TRUST_MARK, required = false) final String trustMark,
                                          final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        val error = verifyRequest(request, response, OidcConstants.TRUST_MARK_STATUS_FEDERATION_URL);
        if (error != null) {
            return error;
        }
        if (StringUtils.isBlank(trustMark)) {
            return errorResponse(HttpStatus.BAD_REQUEST, OAuth20Constants.INVALID_REQUEST, "Missing trust mark");
        }
        try {
            return trustMarkIssuer.status(trustMark)
                .map(status -> ResponseEntity.ok()
                    .cacheControl(CacheControl.noStore())
                    .contentType(OidcConstants.TRUST_MARK_STATUS_RESPONSE_CONTENT_TYPE)
                    .body((Object) status.serialize()))
                .orElseGet(() -> errorResponse(HttpStatus.NOT_FOUND, NOT_FOUND, "Trust mark was not issued by this entity"));
        } catch (final ParseException e) {
            LOGGER.debug("Trust mark cannot be parsed: [{}]", e.getMessage());
            return errorResponse(HttpStatus.BAD_REQUEST, OAuth20Constants.INVALID_REQUEST, "Trust mark cannot be parsed");
        }
    }

    /**
     * List the entities that hold a trust mark of the given type.
     *
     * @param trustMarkType the trust mark type
     * @param subject       the entity identifier to filter on
     * @param request       the request
     * @param response      the response
     * @return the entity identifiers
     */
    @GetMapping('/' + OidcConstants.BASE_OIDC_URL + OidcConstants.TRUST_MARK_LIST_FEDERATION_URL)
    @Operation(summary = "Handle OIDC federation trust marked entities listing request",
        description = "Lists the entities that hold a trust mark of the requested type",
        parameters = {
            @Parameter(name = TRUST_MARK_TYPE, description = "Trust mark type", required = true),
            @Parameter(name = SUBJECT, description = "Entity identifier to filter on")
        })
    public ResponseEntity listTrustMarkedEntities(@RequestParam(value = TRUST_MARK_TYPE, required = false) final String trustMarkType,
                                                  @RequestParam(value = SUBJECT, required = false) final String subject,
                                                  final HttpServletRequest request, final HttpServletResponse response) {
        val error = verifyRequest(request, response, OidcConstants.TRUST_MARK_LIST_FEDERATION_URL);
        if (error != null) {
            return error;
        }
        if (StringUtils.isBlank(trustMarkType)) {
            return errorResponse(HttpStatus.BAD_REQUEST, OAuth20Constants.INVALID_REQUEST, "Missing trust mark type");
        }
        return trustMarkIssuer.list(trustMarkType, subject)
            .map(entities -> ResponseEntity.ok().cacheControl(CacheControl.noStore()).body((Object) entities))
            .orElseGet(() -> errorResponse(HttpStatus.NOT_FOUND, NOT_FOUND, "This entity issues no trust marks of this type"));
    }

    private @Nullable ResponseEntity verifyRequest(final HttpServletRequest request, final HttpServletResponse response, final String path) {
        if (!trustMarkIssuer.isTrustMarkIssuer()) {
            return errorResponse(HttpStatus.NOT_FOUND, NOT_FOUND, "This entity issues no trust marks");
        }
        return retrieveInvalidIssuerError(request, response, path);
    }
}

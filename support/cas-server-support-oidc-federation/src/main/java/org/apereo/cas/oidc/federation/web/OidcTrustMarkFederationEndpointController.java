package org.apereo.cas.oidc.federation.web;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.federation.signature.OidcFederationEntityStatementService;
import org.apereo.cas.oidc.federation.trustmark.OidcFederationTrustMarkIssuer;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The endpoints of CAS as a trust mark issuer (OpenID Federation 1.0): the trust mark endpoint (section 8.6), the
 * trust mark status endpoint (section 8.4) and the trust marked entities listing endpoint (section 8.5). Requests are
 * not authenticated, so the trust mark endpoint is a {@code GET} and the status endpoint a {@code POST} of
 * {@code trust_mark}. Errors are JSON (section 8.9): a missing parameter is {@code invalid_request} with 400, and an
 * entity that does not hold the requested trust mark, a trust mark this entity did not issue, or a type it does not
 * issue is {@code not_found} with 404, as is every request when this entity issues no trust marks.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
public class OidcTrustMarkFederationEndpointController extends AbstractOidcFederationEndpointController {
    private static final String NOT_FOUND = "not_found";

    private static final String TRUST_MARK_TYPE = "trust_mark_type";

    private static final String TRUST_MARK = "trust_mark";

    private static final String SUBJECT = "sub";

    private final OidcFederationTrustMarkIssuer trustMarkIssuer;

    public OidcTrustMarkFederationEndpointController(final OidcIssuerService oidcIssuerService,
                                                     final OidcFederationEntityStatementService federationEntityStatementService,
                                                     final OidcProperties oidcProperties,
                                                     final OidcFederationTrustMarkIssuer trustMarkIssuer) {
        super(oidcIssuerService, federationEntityStatementService, oidcProperties);
        this.trustMarkIssuer = trustMarkIssuer;
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
        if (StringUtils.isAnyBlank(trustMarkType, subject)) {
            return errorResponse(HttpStatus.BAD_REQUEST, OAuth20Constants.INVALID_REQUEST, "Missing trust mark type or subject");
        }
        return trustMarkIssuer.issue(trustMarkType, subject)
            .map(trustMark -> ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(OidcConstants.TRUST_MARK_CONTENT_TYPE)
                .body((Object) trustMark.serialize()))
            .orElseGet(() -> errorResponse(HttpStatus.NOT_FOUND, NOT_FOUND, "Entity does not hold a trust mark of this type"));
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

    private static ResponseEntity errorResponse(final HttpStatus status, final String error, final String description) {
        return new ResponseEntity<>(OAuth20Utils.getErrorResponseBody(error, description), status);
    }
}

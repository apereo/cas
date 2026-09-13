package org.apereo.cas.oidc.vc.issuer.web;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.authz.OidcVerifiableCredentialAuthorizationDetails;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialIssuerService;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialRequest;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialResponse;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialValidationContext;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.Couplet;
import org.apereo.cas.util.LoggingUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.pac4j.core.context.WebContext;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcVerifiableCredentialEndpointController}.
 * <p>
 * OpenID4VCI 1.0 has a single credential endpoint. A request carrying several proofs is a batch
 * request, and the response carries one credential per proof; the separate batch credential
 * endpoint of earlier drafts no longer exists.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag(name = "OpenID Connect")
@Slf4j
public class OidcVerifiableCredentialEndpointController extends BaseOAuth20Controller<OidcConfigurationContext> {
    protected final OidcVerifiableCredentialIssuerService credentialIssuerService;

    public OidcVerifiableCredentialEndpointController(
        final OidcConfigurationContext configurationContext,
        final OidcVerifiableCredentialIssuerService credentialIssuerService) {
        super(configurationContext);
        this.credentialIssuerService = credentialIssuerService;
    }

    /**
     * Handle response entity.
     *
     * @param request      the credential request
     * @param httpRequest  the http request
     * @param httpResponse the http response
     * @return the response entity
     * @throws Throwable the throwable
     */
    @PostMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_CREDENTIAL_URL,
        "/**/" + OidcConstants.VC_CREDENTIAL_URL
    }, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handle OIDC credential request",
        description = "Handles requests for OIDC credential issuance")
    public ResponseEntity handle(
        @RequestBody final OidcVerifiableCredentialRequest request,
        final HttpServletRequest httpRequest,
        final HttpServletResponse httpResponse) throws Throwable {

        val verified = verifyRequest(httpRequest, httpResponse);
        if (verified.getRight() != null) {
            return verified.getRight();
        }
        val decodedToken = Objects.requireNonNull(verified.getLeft());

        val identifierError = validateCredentialIdentifiers(request, decodedToken);
        if (identifierError != null) {
            return identifierError;
        }

        val issuanceContext = new OidcVerifiableCredentialValidationContext(decodedToken, request, httpRequest);
        val batchError = validateBatchSize(issuanceContext);
        if (batchError != null) {
            return batchError;
        }

        if (!validateAccessTokenForCredentialIssuance(decodedToken, issuanceContext)) {
            return ResponseEntity.badRequest()
                .body(OAuth20Utils.getErrorResponseBody(OAuth20Constants.ERROR,
                    "Access token cannot be accepted for credential issuance"));
        }

        val issuedCredentials = credentialIssuerService.issue(issuanceContext, new HashSet<>());
        val credentials = issuedCredentials
            .stream()
            .<OidcVerifiableCredentialResponse.IssuedCredential>map(issued -> OidcVerifiableCredentialResponse.IssuedCredential
                .builder()
                .credential(issued.credential())
                .build())
            .toList();
        val response = OidcVerifiableCredentialResponse.builder().credentials(credentials).build();
        return ResponseEntity.ok(response);
    }

    /**
     * A credential identifier is only meaningful when the token response advertised one, and the
     * two request parameters are mutually exclusive.
     *
     * @param request     the credential request
     * @param accessToken the access token
     * @return an error response, or null when the request is acceptable
     */
    protected @Nullable ResponseEntity validateCredentialIdentifiers(final OidcVerifiableCredentialRequest request,
                                                                     final OAuth20AccessToken accessToken) {
        val hasIdentifier = StringUtils.isNotBlank(request.getCredentialIdentifier());
        val hasConfigurationId = StringUtils.isNotBlank(request.getCredentialConfigurationId());
        if (hasIdentifier && hasConfigurationId) {
            return ResponseEntity.badRequest()
                .body(OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST,
                    "Only one of credential_identifier or credential_configuration_id may be specified"));
        }
        if (hasIdentifier && !accessToken.hasAuthorizationDetails()) {
            return ResponseEntity.badRequest()
                .body(OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST,
                    "A credential identifier cannot be used with an access token that carries no authorization details"));
        }
        return null;
    }

    protected @Nullable ResponseEntity validateBatchSize(final OidcVerifiableCredentialValidationContext context) {
        val maximumBatchSize = Math.max(1, getConfigurationContext().getCasProperties()
            .getAuthn().getOidc().getVc().getIssuer().getBatchSize());
        if (context.resolveProofs().size() > maximumBatchSize) {
            return ResponseEntity.badRequest()
                .body(OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST,
                    "Credential batch size is invalid"));
        }
        return null;
    }

    protected boolean validateAccessTokenForCredentialIssuance(final OAuth20AccessToken accessToken,
                                                               final OidcVerifiableCredentialValidationContext issuanceContext) {
        val authorizedConfigurationIds = resolveAuthorizedCredentialConfigurationIds(accessToken);
        if (authorizedConfigurationIds.isEmpty()) {
            LOGGER.warn("Access token does not authorize any credential configuration");
            return false;
        }
        return authorizedConfigurationIds.contains(issuanceContext.resolveConfigurationId());
    }

    /**
     * Credential configurations the access token is allowed to request. Tokens issued through the
     * pre-authorized code flow carry the identifiers directly, while the authorization code flow
     * records them as authorization details attached to the token.
     *
     * @param accessToken the access token
     * @return the authorized credential configuration ids, never null
     */
    protected List<String> resolveAuthorizedCredentialConfigurationIds(final OAuth20AccessToken accessToken) {
        val configurationIds = accessToken.getCredentialConfigurationIds();
        if (configurationIds != null && !configurationIds.isEmpty()) {
            return List.copyOf(configurationIds);
        }
        val authorizationDetails = accessToken.getAuthorizationDetails();
        if (authorizationDetails == null) {
            return List.of();
        }
        return authorizationDetails
            .stream()
            .map(OidcVerifiableCredentialEndpointController::toCredentialConfigurationId)
            .filter(StringUtils::isNotBlank)
            .toList();
    }

    private static String toCredentialConfigurationId(final Serializable authorizationDetails) {
        return switch (authorizationDetails) {
            case final OidcVerifiableCredentialAuthorizationDetails details -> details.getCredentialConfigurationId();
            case final Map<?, ?> details -> Objects.toString(details.get("credential_configuration_id"), StringUtils.EMPTY);
            default -> StringUtils.EMPTY;
        };
    }

    protected Couplet<@Nullable OAuth20AccessToken, @Nullable ResponseEntity> verifyRequest(
        final HttpServletRequest httpRequest,
        final HttpServletResponse httpResponse) {
        val webContext = new JEEContext(httpRequest, httpResponse);
        if (!getConfigurationContext().getIssuerService().validateIssuer(webContext, List.of(OidcConstants.VC_CREDENTIAL_URL))) {
            LOGGER.warn("CAS cannot accept the request given the issuer is invalid.");
            val body = OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "Invalid issuer");
            return Couplet.right(ResponseEntity.badRequest().body(body));
        }

        val presentedAccessToken = getAccessTokenFromRequest(httpRequest);
        val decodedToken = getConfigurationContext().getTicketRegistry()
            .getTicket(presentedAccessToken.getValue(), OAuth20AccessToken.class);
        if (!validateAccessToken(decodedToken)) {
            LOGGER.warn("The access token is invalid, expired, has an invalid grant type or no authorization details.");
            return Couplet.right(ResponseEntity.badRequest()
                .body(OAuth20Utils.getErrorResponseBody(OAuth20Constants.ERROR, "Invalid access token")));
        }
        val proofError = verifyProofOfPossession(webContext, presentedAccessToken.getKey(), decodedToken);
        return proofError != null ? Couplet.right(proofError) : Couplet.left(decodedToken);
    }

    /**
     * A credential request may carry a sender-constrained access token: OpenID4VCI 1.0, section 7.2
     * speaks of the Wallet using a nonce "in the DPoP proof when presenting an access token at the
     * Credential Endpoint". Accepting such a token on presentation alone would leave the constraint
     * doing nothing, so the proof is verified here against the confirmation recorded at issuance.
     * A token with no confirmation is an ordinary bearer token and passes straight through.
     *
     * @param webContext           the web context
     * @param presentedAccessToken the access token exactly as the wallet presented it
     * @param accessToken          the access token ticket
     * @return an error response when the proof is missing or does not verify, otherwise null
     */
    protected @Nullable ResponseEntity verifyProofOfPossession(final WebContext webContext,
                                                               final String presentedAccessToken,
                                                               final OAuth20AccessToken accessToken) {
        try {
            getConfigurationContext().getProofOfPossessionValidator()
                .validateProtectedResourceRequest(webContext, presentedAccessToken, accessToken);
            return null;
        } catch (final Throwable e) {
            LoggingUtils.warn(LOGGER, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE,
                    "%s error=\"%s\"".formatted(OAuth20Constants.TOKEN_TYPE_DPOP, OAuth20Constants.INVALID_DPOP_PROOF))
                .body(OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_DPOP_PROOF, e.getMessage()));
        }
    }

    protected boolean validateAccessToken(@Nullable final OAuth20AccessToken accessToken) {
        return accessToken != null && !accessToken.isExpired()
            && (accessToken.getGrantType() == OAuth20GrantTypes.PRE_AUTHORIZED_CODE || accessToken.hasAuthorizationDetails());
    }

    /**
     * Handle errors.
     *
     * @param ex the ex
     * @return the response entity
     */
    @ExceptionHandler(Exception.class)
    @SuppressWarnings("UnusedMethod")
    private static ResponseEntity handleErrors(final Exception ex) {
        LoggingUtils.error(LOGGER, ex);
        if (ex instanceof final ResponseStatusException rse) {
            return ResponseEntity
                .status(rse.getStatusCode())
                .body(OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, rse.getReason()));
        }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, ex.getMessage()));
    }
}

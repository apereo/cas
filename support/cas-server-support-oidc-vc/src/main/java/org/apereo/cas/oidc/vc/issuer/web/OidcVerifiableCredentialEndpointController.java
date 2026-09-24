package org.apereo.cas.oidc.vc.issuer.web;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.authz.OidcVerifiableCredentialAuthorizationDetails;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialIssuerService;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialRequest;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialResponse;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialValidationContext;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofException;
import org.apereo.cas.oidc.vc.services.OidcVerifiableCredentialPolicyUtils;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.Couplet;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
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

        val issuanceError = validateCredentialIssuance(decodedToken, issuanceContext);
        if (issuanceError != null) {
            return issuanceError;
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
            return badRequest(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST,
                "Only one of credential_identifier or credential_configuration_id may be specified");
        }
        if (hasIdentifier && !accessToken.hasAuthorizationDetails()) {
            return badRequest(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST,
                "A credential identifier cannot be used with an access token that carries no authorization details");
        }
        return null;
    }

    protected @Nullable ResponseEntity validateBatchSize(final OidcVerifiableCredentialValidationContext context) {
        val maximumBatchSize = Math.max(1, getConfigurationContext().getCasProperties()
            .getAuthn().getOidc().getVc().getIssuer().getBatchSize());
        if (context.resolveProofs().size() > maximumBatchSize) {
            return badRequest(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST, "Credential batch size is invalid");
        }
        return null;
    }

    /**
     * Decide whether this token may obtain this credential, and say why not in the vocabulary
     * OpenID4VCI defines for the credential endpoint: a credential the issuer does not publish is
     * {@code unsupported_credential_type}, while one it publishes but will not hand to this caller is
     * {@code credential_request_denied}. A wallet can act on the difference; a single opaque error
     * tells it only to stop.
     *
     * @param accessToken      the access token
     * @param issuanceContext  the issuance context
     * @return an error response, or null when issuance may proceed
     */
    protected @Nullable ResponseEntity validateCredentialIssuance(final OAuth20AccessToken accessToken,
                                                                   final OidcVerifiableCredentialValidationContext issuanceContext) {
        val requestedConfigurationId = issuanceContext.resolveConfigurationId();
        val publishedConfigurationIds = getConfigurationContext().getCasProperties()
            .getAuthn().getOidc().getVc().getIssuer().getCredentialConfigurations().keySet();
        if (!publishedConfigurationIds.contains(requestedConfigurationId)) {
            LOGGER.warn("Credential configuration [{}] is not published by this issuer", requestedConfigurationId);
            return badRequest(OidcConstants.VC_ERROR_UNSUPPORTED_CREDENTIAL_TYPE,
                "Credential configuration %s is not supported".formatted(requestedConfigurationId));
        }

        val authorizedConfigurationIds = resolveAuthorizedCredentialConfigurationIds(accessToken);
        if (!authorizedConfigurationIds.contains(requestedConfigurationId)) {
            LOGGER.warn("Access token authorizes [{}] and does not cover [{}]",
                authorizedConfigurationIds, requestedConfigurationId);
            return badRequest(OidcConstants.VC_ERROR_CREDENTIAL_REQUEST_DENIED,
                "Access token does not authorize credential configuration %s".formatted(requestedConfigurationId));
        }

        /*
         * What the token authorizes was decided when the token was minted. The service policy is read
         * again here rather than trusted from then, so that tightening a relying party's verifiable
         * credentials policy takes effect against tokens that are already outstanding.
         */
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            getConfigurationContext().getServicesManager(), accessToken.getClientId(), OidcRegisteredService.class);
        val allowedConfigurationIds = OidcVerifiableCredentialPolicyUtils.resolveAllowedCredentialConfigurationIds(
            registeredService, publishedConfigurationIds);
        if (!allowedConfigurationIds.contains(requestedConfigurationId)) {
            LOGGER.warn("Client [{}] is not allowed to obtain credential configuration [{}]; allowed: [{}]",
                accessToken.getClientId(), requestedConfigurationId, allowedConfigurationIds);
            return badRequest(OidcConstants.VC_ERROR_CREDENTIAL_REQUEST_DENIED,
                "Credential configuration %s is not permitted for this client".formatted(requestedConfigurationId));
        }
        return null;
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
            return Couplet.right(badRequest(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST, "Invalid issuer"));
        }

        val presentedAccessToken = getAccessTokenFromRequest(httpRequest);
        if (StringUtils.isBlank(presentedAccessToken.getKey())) {
            LOGGER.warn("The credential request carries no access token in its authorization header.");
            return Couplet.right(unauthorized(httpRequest, null, null));
        }
        val decodedToken = FunctionUtils.doAndHandle(() -> getConfigurationContext().getTicketRegistry()
            .getTicket(presentedAccessToken.getValue(), OAuth20AccessToken.class));
        if (decodedToken == null || decodedToken.isExpired()) {
            LOGGER.warn("The access token presented to the credential endpoint is unknown or expired.");
            return Couplet.right(unauthorized(httpRequest, OAuth20Constants.INVALID_TOKEN,
                "The access token is invalid, revoked or expired"));
        }
        val proofError = verifyProofOfPossession(webContext, presentedAccessToken.getKey(), decodedToken);
        if (proofError != null) {
            return Couplet.right(proofError);
        }
        if (!validateAccessToken(decodedToken)) {
            LOGGER.warn("The access token has an invalid grant type or carries no authorization details.");
            return Couplet.right(badRequest(OidcConstants.VC_ERROR_CREDENTIAL_REQUEST_DENIED,
                "Access token does not authorize credential issuance"));
        }
        return Couplet.left(decodedToken);
    }

    /**
     * The credential endpoint is an OAuth protected resource, so a token that cannot be accepted is a
     * 401 carrying a {@code WWW-Authenticate} challenge, not a 400. RFC 9110, section 15.5.2 makes the
     * challenge mandatory on a 401, and RFC 6750, section 3 says to name the error only when the
     * request actually presented credentials -- a client that sent none is told which scheme to use
     * and nothing more. The challenge answers in whichever scheme the client used, so a DPoP-bound
     * token is not told to retry as a bearer token.
     *
     * @param request     the request
     * @param error       the error code, or null when the request carried no token at all
     * @param description the error description
     * @return the response entity
     */
    protected ResponseEntity unauthorized(final HttpServletRequest request,
                                          final @Nullable String error,
                                          final @Nullable String description) {
        val challenge = new StringBuilder(resolveAuthorizationScheme(request));
        if (StringUtils.isNotBlank(error)) {
            challenge.append(" error=\"").append(error).append('"');
            if (StringUtils.isNotBlank(description)) {
                challenge.append(", error_description=\"").append(toChallengeValue(description)).append('"');
            }
        }
        val response = ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .header(HttpHeaders.WWW_AUTHENTICATE, challenge.toString());
        return StringUtils.isBlank(error)
            ? response.build()
            : response.body(OAuth20Utils.getErrorResponseBody(error, description));
    }

    /**
     * Challenge parameters are quoted strings, so anything that would end the quoted string early --
     * a quote, a backslash or a control character -- is removed rather than escaped.
     *
     * @param value the value
     * @return the sanitized value
     */
    protected static String toChallengeValue(final String value) {
        return value
            .chars()
            .filter(character -> character >= ' ' && character != '"' && character != '\\' && !Character.isISOControl(character))
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }

    protected static ResponseEntity badRequest(final String error, final String description) {
        return ResponseEntity.badRequest().body(OAuth20Utils.getErrorResponseBody(error, description));
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
            val description = StringUtils.defaultIfBlank(e.getMessage(), "DPoP proof validation failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "%s error=\"%s\", error_description=\"%s\"".formatted(
                    OAuth20Constants.TOKEN_TYPE_DPOP, OAuth20Constants.INVALID_DPOP_PROOF, toChallengeValue(description)))
                .body(OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_DPOP_PROOF, description));
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
        /*
         * A failure raised while validating a proof knows which of the two OpenID4VCI proof errors it
         * is, and the distinction matters to the wallet: invalid_nonce is recoverable by fetching a
         * fresh nonce, invalid_proof is not.
         */
        val proofException = findProofException(ex);
        if (proofException != null) {
            return badRequest(proofException.getError(), proofException.getMessage());
        }
        if (ex instanceof final ResponseStatusException rse) {
            return ResponseEntity
                .status(rse.getStatusCode())
                .body(OAuth20Utils.getErrorResponseBody(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST, rse.getReason()));
        }
        return badRequest(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST, ex.getMessage());
    }

    private static @Nullable OidcVerifiableCredentialProofException findProofException(final Throwable throwable) {
        var cause = throwable;
        while (cause != null) {
            if (cause instanceof final OidcVerifiableCredentialProofException proofException) {
                return proofException;
            }
            cause = Objects.equals(cause.getCause(), cause) ? null : cause.getCause();
        }
        return null;
    }
}

package org.apereo.cas.oidc.vc.issuer.web;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialIssuerService;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialRequest;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialResponse;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialValidationContext;
import org.apereo.cas.oidc.vc.issuer.nonce.OidcVerifiableCredentialNonceService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.Couplet;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

/**
 * This is {@link OidcVerifiableCredentialEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag(name = "OpenID Connect")
@Slf4j
public class OidcVerifiableCredentialEndpointController extends BaseOAuth20Controller<OidcConfigurationContext> {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    protected final OidcVerifiableCredentialIssuerService credentialIssuerService;
    protected final OidcVerifiableCredentialNonceService oidcVerifiableCredentialNonceService;

    public OidcVerifiableCredentialEndpointController(
        final OidcConfigurationContext configurationContext,
        final OidcVerifiableCredentialIssuerService credentialIssuerService,
        final OidcVerifiableCredentialNonceService oidcVerifiableCredentialNonceService) {
        super(configurationContext);
        this.credentialIssuerService = credentialIssuerService;
        this.oidcVerifiableCredentialNonceService = oidcVerifiableCredentialNonceService;
    }

    /**
     * Handle batch response entity.
     *
     * @param batchRequest the batch request
     * @param httpRequest  the http request
     * @param httpResponse the http response
     * @return the response entity
     * @throws Throwable the throwable
     */
    @PostMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_BATCH_CREDENTIAL_URL,
        "/**/" + OidcConstants.VC_BATCH_CREDENTIAL_URL
    }, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handle OIDC batch credential request",
        description = "Handles requests for OIDC batch credential issuance")
    public ResponseEntity handleBatch(
        @RequestBody final OidcVcBatchCredentialRequest batchRequest,
        final HttpServletRequest httpRequest,
        final HttpServletResponse httpResponse) throws Throwable {

        val verified = verifyRequest(httpRequest, httpResponse);
        if (verified.getRight() != null) {
            return verified.getRight();
        }
        val decodedToken = verified.getLeft();

        val responses = new ArrayList<>();
        val nonces = new HashSet<String>();

        for (val credentialRequest : batchRequest.credentialRequests()) {
            val issuanceContext = new OidcVerifiableCredentialValidationContext(
                Objects.requireNonNull(decodedToken), credentialRequest, httpRequest);
            val issuedCredentials = credentialIssuerService.issue(issuanceContext);
            for (val issuedCredential : issuedCredentials) {
                nonces.add(issuedCredential.nonce());
                responses.add(OidcVerifiableCredentialResponse
                    .builder()
                    .format(issuedCredential.format().getValue())
                    .credential(issuedCredential.credential())
                    .build());
            }
        }
        nonces.forEach(oidcVerifiableCredentialNonceService::remove);
        return ResponseEntity.ok(Map.of("credential_responses", responses));
    }

    /**
     * Handle response entity.
     *
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
        @RequestBody final JsonNode body,
        final HttpServletRequest httpRequest,
        final HttpServletResponse httpResponse) throws Throwable {

        if (body.has("credential_requests")) {
            val batch = MAPPER.treeToValue(body, OidcVcBatchCredentialRequest.class);
            return handleBatch(batch, httpRequest, httpResponse);
        }

        val verified = verifyRequest(httpRequest, httpResponse);
        if (verified.getRight() != null) {
            return verified.getRight();
        }
        val decodedToken = verified.getLeft();
        val request = MAPPER.treeToValue(body, OidcVerifiableCredentialRequest.class);
        val issuanceContext = new OidcVerifiableCredentialValidationContext(
            Objects.requireNonNull(decodedToken), request, httpRequest);
        val issuerResponses = credentialIssuerService.issue(issuanceContext);

        val responses = new ArrayList<OidcVerifiableCredentialResponse>();
        val nonces = new HashSet<String>();
        for (val issuedCredential : issuerResponses) {
            nonces.add(issuedCredential.nonce());
            responses.add(OidcVerifiableCredentialResponse
                .builder()
                .format(issuedCredential.format().getValue())
                .credential(issuedCredential.credential())
                .build());
        }
        nonces.forEach(oidcVerifiableCredentialNonceService::remove);
        return responses.size() == 1
            ? ResponseEntity.ok(responses.getFirst())
            : ResponseEntity.ok(Map.of("credential_responses", responses));
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

        val decodedAccessTokenId = getAccessTokenFromRequest(httpRequest).getValue();
        val decodedToken = getConfigurationContext().getTicketRegistry().getTicket(decodedAccessTokenId, OAuth20AccessToken.class);
        if (!validateAccessToken(decodedToken)) {
            LOGGER.warn("The access token is invalid, expired, has an invalid grant type or no authorization details.");
            return Couplet.right(ResponseEntity.badRequest()
                .body(OAuth20Utils.getErrorResponseBody(OAuth20Constants.ERROR, "Invalid access token")));
        }
        return Couplet.left(decodedToken);
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

    public record OidcVcBatchCredentialRequest(
        @JsonProperty("credential_requests")
        @NotEmpty
        List<@Valid OidcVerifiableCredentialRequest> credentialRequests) {
    }
}

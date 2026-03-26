package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.LoggingUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link OidcCredentialEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag(name = "OpenID Connect")
@Slf4j
public class OidcCredentialEndpointController extends BaseOAuth20Controller<OidcConfigurationContext> {

    private final OidcVerifiableCredentialIssuerService credentialIssuerService;

    public OidcCredentialEndpointController(final OidcConfigurationContext configurationContext,
                                            final OidcVerifiableCredentialIssuerService credentialIssuerService) {
        super(configurationContext);
        this.credentialIssuerService = credentialIssuerService;
    }

    /**
     * Handle response entity.
     *
     * @param request     the request
     * @param httpRequest the http request
     * @return the response entity
     */
    @GetMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_CREDENTIAL_URL,
        "/**/" + OidcConstants.VC_CREDENTIAL_URL
    }, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handle OIDC credential request",
        description = "Handles requests for OIDC credential issuance")
    public ResponseEntity<VerifiableCredentialResponse> handle(
        @RequestBody final VerifiableCredentialRequest request,
        final HttpServletRequest httpRequest) {
        val decodedAccessTokenId = getAccessTokenFromRequest(httpRequest).getValue();
        val decodedToken = getConfigurationContext().getTicketRegistry().getTicket(decodedAccessTokenId, OAuth20AccessToken.class);
        val issuanceContext = new OidcVerifiableCredentialIssuerService.CredentialRequestValidationContext(
            Objects.requireNonNull(decodedToken), request, httpRequest);
        val response = credentialIssuerService.issue(issuanceContext);
        return ResponseEntity.ok().body(response);
    }

    /**
     * Handle errors.
     *
     * @param ex the ex
     * @return the response entity
     */
    @ExceptionHandler(Exception.class)
    @SuppressWarnings("UnusedMethod")
    private static ResponseEntity<String> handle(final Exception ex) {
        LoggingUtils.error(LOGGER, ex);
        if (ex instanceof final ResponseStatusException rse) {
            return ResponseEntity.status(rse.getStatusCode()).body(rse.getReason());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
